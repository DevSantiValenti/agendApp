package com.analistas.agendApp.service.impl;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.analistas.agendApp.dto.TurnoEmailData;
import com.analistas.agendApp.model.Turno;
import com.analistas.agendApp.service.ITurnoEmailService;

import jakarta.mail.internet.MimeMessage;

@Service
public class TurnoEmailServiceImpl implements ITurnoEmailService {
	private static final Logger log = LoggerFactory.getLogger(TurnoEmailServiceImpl.class);
	private static final String UBICACION = "Av Alvear 856";
	private static final String MAPS_URL = "https://www.google.com/maps/search/?api=1&query=Av%20Alvear%20856";
	private static final DateTimeFormatter FECHA_LARGA = DateTimeFormatter
			.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "AR"));
	private static final DateTimeFormatter HORA_CORTA = DateTimeFormatter.ofPattern("HH:mm");

	private final ObjectProvider<JavaMailSender> mailSenderProvider;
	private final Environment environment;

	public TurnoEmailServiceImpl(ObjectProvider<JavaMailSender> mailSenderProvider, Environment environment) {
		this.mailSenderProvider = mailSenderProvider;
		this.environment = environment;
	}

	@Override
	public void notificarAsignacion(Turno turno, String asignadoPor) {
		TurnoEmailData data = TurnoEmailData.desde(turno, asignadoPor);
		// Envío al profesional pausado por ahora. Se conserva el código preparado para reactivarlo.
		// enviarProfesional(data, "Turno asignado",
		// 		"Agenda Uno te avisa que tenés un turno asignado",
		// 		"Se asignó un turno en tu agenda.", "Asignado por", "#3d7df0");
		enviarPaciente(data);
	}

	@Override
	public void notificarAnulacion(TurnoEmailData turno) {
		// Envío al profesional pausado por ahora. No se envía mail al anular turnos.
		// enviarProfesional(turno, "Turno anulado",
		// 		"Agenda Uno te avisa que se anuló un turno",
		// 		"Se anuló un turno que estaba asignado en tu agenda.", "Anulado por", "#ef4444");
	}

	private void enviarProfesional(TurnoEmailData turno, String titulo, String encabezado, String bajada,
			String actorLabel, String color) {
		enviar(turno, turno == null ? null : turno.profesionalEmail(), titulo,
				htmlProfesional(turno, encabezado, bajada, actorLabel, color));
	}

	private void enviarPaciente(TurnoEmailData turno) {
		if (turno == null || !turno.tienePacienteEmail()) {
			log.debug("No se envía mail de turno al paciente porque no tiene email cargado.");
			return;
		}
		enviar(turno, turno.pacienteEmail(), "Confirmación de turno",
				htmlPaciente(turno));
	}

	private void enviar(TurnoEmailData turno, String destinatario, String titulo, String html) {
		JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
		if (mailSender == null || !StringUtils.hasText(environment.getProperty("spring.mail.host"))) {
			log.debug("No se envía mail de turno porque spring.mail.host no está configurado.");
			return;
		}
		if (turno == null || !StringUtils.hasText(destinatario)) {
			log.debug("No se envía mail de turno porque falta destinatario.");
			return;
		}
		String from = remitente();
		if (!StringUtils.hasText(from)) {
			log.warn("No se envía mail de turno porque falta configurar app.mail.from o spring.mail.username.");
			return;
		}
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
			helper.setFrom(from);
			helper.setTo(destinatario.trim());
			helper.setSubject("Agenda Uno - " + titulo);
			helper.setText(html, true);
			mailSender.send(message);
		} catch (Exception ex) {
			log.warn("No se pudo enviar el mail de notificación de turno a {}", destinatario, ex);
		}
	}

	private String remitente() {
		String from = environment.getProperty("app.mail.from");
		return StringUtils.hasText(from) ? from : environment.getProperty("spring.mail.username");
	}

	private String htmlProfesional(TurnoEmailData turno, String encabezado, String bajada, String actorLabel, String color) {
		String fecha = turno.dia() == null ? "-" : turno.dia().format(FECHA_LARGA);
		String hora = turno.hora() == null ? "-" : turno.hora().format(HORA_CORTA);
		return """
				<!doctype html>
				<html lang="es">
				<body style="margin:0;background:#eef2f7;font-family:Segoe UI,Arial,sans-serif;color:#1d2738;">
					<table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background:#eef2f7;padding:28px 12px;">
						<tr>
							<td align="center">
								<table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="max-width:620px;background:#ffffff;border-radius:14px;overflow:hidden;border:1px solid #dfe5ef;">
									<tr>
										<td style="background:%s;padding:26px 30px;color:#ffffff;">
											<div style="font-size:13px;font-weight:700;letter-spacing:.08em;text-transform:uppercase;">Agenda Uno</div>
											<h1 style="margin:10px 0 0;font-size:25px;line-height:1.25;">%s</h1>
											<p style="margin:10px 0 0;font-size:15px;opacity:.94;">%s</p>
										</td>
									</tr>
									<tr>
										<td style="padding:26px 30px;">
											<p style="margin:0 0 18px;font-size:16px;">Hola <strong>%s</strong>,</p>
											<table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="border-collapse:separate;border-spacing:0 10px;">
												%s
												%s
												%s
												%s
												%s
											</table>
										</td>
									</tr>
									<tr>
										<td style="padding:18px 30px;background:#f8fbff;color:#6b7b92;font-size:13px;">
											Este aviso fue generado automáticamente por Agenda Uno.
										</td>
									</tr>
								</table>
							</td>
						</tr>
					</table>
				</body>
				</html>
				""".formatted(
				color,
				escape(encabezado),
				escape(bajada),
				escape(valor(turno.profesionalNombre())),
				fila("Día", fecha),
				fila("Hora", hora),
				fila("Paciente", valor(turno.pacienteNombre())),
				fila(actorLabel, valor(turno.actor())),
				fila("Observación", valor(turno.observacion())));
	}

	private String htmlPaciente(TurnoEmailData turno) {
		String fecha = turno.dia() == null ? "-" : turno.dia().format(FECHA_LARGA);
		String hora = turno.hora() == null ? "-" : turno.hora().format(HORA_CORTA);
		return """
				<!doctype html>
				<html lang="es">
				<body style="margin:0;background:#eef2f7;font-family:Segoe UI,Arial,sans-serif;color:#1d2738;">
					<table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background:#eef2f7;padding:28px 12px;">
						<tr>
							<td align="center">
								<table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="max-width:620px;background:#ffffff;border-radius:14px;overflow:hidden;border:1px solid #dfe5ef;">
									<tr>
										<td style="background:#3d7df0;padding:26px 30px;color:#ffffff;">
											<div style="font-size:13px;font-weight:700;letter-spacing:.08em;text-transform:uppercase;">Agenda Uno</div>
											<h1 style="margin:10px 0 0;font-size:25px;line-height:1.25;">Confirmación de turno</h1>
											<p style="margin:10px 0 0;font-size:15px;opacity:.94;">Te confirmamos que tenés un turno asignado.</p>
										</td>
									</tr>
									<tr>
										<td style="padding:26px 30px;">
											<p style="margin:0 0 18px;font-size:16px;">Hola <strong>%s</strong>,</p>
											<table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="border-collapse:separate;border-spacing:0 10px;">
												%s
												%s
												%s
												%s
												%s
											</table>
											<a href="%s" style="display:inline-block;margin-top:14px;background:#3d7df0;color:#ffffff;text-decoration:none;padding:13px 18px;border-radius:10px;font-weight:700;">Ver ubicación en Maps</a>
										</td>
									</tr>
									<tr>
										<td style="padding:18px 30px;background:#f8fbff;color:#6b7b92;font-size:13px;">
											Este aviso fue generado automáticamente por Agenda Uno.
										</td>
									</tr>
								</table>
							</td>
						</tr>
					</table>
				</body>
				</html>
				""".formatted(
				escape(valor(turno.pacienteNombre())),
				fila("Día", fecha),
				fila("Hora", hora),
				fila("Profesional", valor(turno.profesionalNombre())),
				fila("Ubicación", UBICACION),
				fila("Observación", valor(turno.observacion())),
				MAPS_URL);
	}

	private String fila(String etiqueta, String valor) {
		return """
				<tr>
					<td style="width:42%%;padding:13px 14px;background:#f8fbff;border:1px solid #e6edf6;border-right:0;border-radius:10px 0 0 10px;color:#6b7b92;font-weight:700;">%s</td>
					<td style="padding:13px 14px;background:#ffffff;border:1px solid #e6edf6;border-left:0;border-radius:0 10px 10px 0;font-weight:700;">%s</td>
				</tr>
				""".formatted(escape(etiqueta), escape(valor));
	}

	private String valor(String value) {
		return StringUtils.hasText(value) ? value : "-";
	}

	private String escape(String value) {
		return value == null ? "" : value
				.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "&quot;")
				.replace("'", "&#39;");
	}
}
