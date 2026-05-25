package com.analistas.agendApp.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.analistas.agendApp.dto.DiaAgenda;
import com.analistas.agendApp.dto.TurnoEmailData;
import com.analistas.agendApp.dto.TurnoAgendaResponse;
import com.analistas.agendApp.dto.TurnoSlot;
import com.analistas.agendApp.model.Paciente;
import com.analistas.agendApp.model.Presentismo;
import com.analistas.agendApp.model.Profesional;
import com.analistas.agendApp.model.ProfesionalHorario;
import com.analistas.agendApp.model.Turno;
import com.analistas.agendApp.repository.IObraSocialRepository;
import com.analistas.agendApp.repository.IPacienteRepository;
import com.analistas.agendApp.repository.IPlanObraSocialRepository;
import com.analistas.agendApp.repository.IProfesionalRepository;
import com.analistas.agendApp.service.IEspecialidadService;
import com.analistas.agendApp.service.IPacienteService;
import com.analistas.agendApp.service.IProfesionalService;
import com.analistas.agendApp.service.ITurnoEmailService;
import com.analistas.agendApp.service.ITurnoService;
import com.analistas.agendApp.service.IUsuarioService;

@Controller
public class TurnoController {
	private static final DateTimeFormatter FECHA_LARGA = DateTimeFormatter
			.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "AR"));
	private static final DateTimeFormatter HORA_CORTA = DateTimeFormatter.ofPattern("HH:mm");
	private static final DateTimeFormatter FECHA_CORTA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static final List<String> DIAS_ATENCION = List.of(
			"Lunes", "Martes", "Miercoles", "Jueves", "Viernes", "Sabado", "Domingo");

	private final ITurnoService turnoService;
	private final IPacienteService pacienteService;
	private final IProfesionalService profesionalService;
	private final IPacienteRepository pacienteRepository;
	private final IProfesionalRepository profesionalRepository;
	private final IObraSocialRepository obraSocialRepository;
	private final IPlanObraSocialRepository planRepository;
	private final IEspecialidadService especialidadService;
	private final IUsuarioService usuarioService;
	private final ITurnoEmailService turnoEmailService;

	public TurnoController(ITurnoService turnoService, IPacienteService pacienteService,
			IProfesionalService profesionalService, IPacienteRepository pacienteRepository,
			IProfesionalRepository profesionalRepository, IObraSocialRepository obraSocialRepository,
			IPlanObraSocialRepository planRepository, IEspecialidadService especialidadService,
			IUsuarioService usuarioService, ITurnoEmailService turnoEmailService) {
		this.turnoService = turnoService;
		this.pacienteService = pacienteService;
		this.profesionalService = profesionalService;
		this.pacienteRepository = pacienteRepository;
		this.profesionalRepository = profesionalRepository;
		this.obraSocialRepository = obraSocialRepository;
		this.planRepository = planRepository;
		this.especialidadService = especialidadService;
		this.usuarioService = usuarioService;
		this.turnoEmailService = turnoEmailService;
	}

	@GetMapping("/turnos")
	public String turnos(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
			@RequestParam(required = false) Long profesionalId,
			@RequestParam(required = false) Long especialidadId, Model model) {
		LocalDate dia = fecha == null ? LocalDate.now() : fecha;
		var profesionales = profesionalService.habilitadosPorEspecialidad(especialidadId);
		Long profesionalSeleccionadoId = seleccionarProfesional(profesionalId, profesionales);
		Profesional profesionalSeleccionado = profesionalSeleccionado(profesionalSeleccionadoId);
		model.addAttribute("fecha", dia);
		model.addAttribute("fechaTexto", dia.format(FECHA_LARGA));
		model.addAttribute("hoy", LocalDate.now());
		model.addAttribute("diasSemana", construirDiasSemana(dia));
		model.addAttribute("profesionalId", profesionalSeleccionadoId);
		model.addAttribute("especialidadId", especialidadId);
		model.addAttribute("slots", turnoService.construirAgenda(dia, profesionalSeleccionadoId));
		model.addAttribute("profesionales", profesionales);
		model.addAttribute("profesionalSeleccionado", profesionalSeleccionado);
		model.addAttribute("horariosProfesionalResumen", resumenHorarios(profesionalSeleccionado));
		model.addAttribute("especialidades", especialidadService.listar());
		model.addAttribute("pacientes", pacienteService.buscar(null));
		model.addAttribute("obrasSociales", obraSocialRepository.findByHabilitadoTrueOrderByNombreAsc());
		model.addAttribute("presentismos", Presentismo.values());
		return "turnos/index";
	}

	private Long seleccionarProfesional(Long profesionalId, List<Profesional> profesionales) {
		if (profesionales == null || profesionales.isEmpty()) {
			return null;
		}
		if (profesionalId != null && profesionales.stream().anyMatch(profesional -> profesionalId.equals(profesional.getId()))) {
			return profesionalId;
		}
		return profesionales.getFirst().getId();
	}

	@GetMapping("/turnos/agenda")
	@ResponseBody
	public TurnoAgendaResponse agenda(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
			@RequestParam(required = false) Long profesionalId) {
		LocalDate dia = fecha == null ? LocalDate.now() : fecha;
		return new TurnoAgendaResponse(
				dia,
				dia.format(FECHA_LARGA),
				construirDiasSemana(dia),
				turnoService.construirAgenda(dia, profesionalId).stream()
						.map(this::toSlotResponse)
						.toList());
	}

	private TurnoAgendaResponse.Slot toSlotResponse(TurnoSlot slot) {
		return new TurnoAgendaResponse.Slot(slot.hora().format(HORA_CORTA),
				slot.turno() == null ? null : toTurnoResponse(slot.turno()));
	}

	private TurnoAgendaResponse.TurnoInfo toTurnoResponse(Turno turno) {
		return new TurnoAgendaResponse.TurnoInfo(
				turno.getId(),
				turno.getPresentismo().name(),
				turno.getPresentismo().getEtiqueta(),
				turno.getPresentismo().getCssClass(),
				turno.getPresentismo().getCodigo(),
				turno.getProfesional() == null ? null : turno.getProfesional().getId(),
				turno.getProfesional() == null ? null : turno.getProfesional().getApellidoNombre(),
				turno.getPaciente() == null ? null : new TurnoAgendaResponse.PacienteInfo(
						turno.getPaciente().getId(),
						turno.getPaciente().getNombreCompleto(),
						turno.getPaciente().getHistoriaClinica(),
						turno.getPaciente().getTelefonoCelular(),
						turno.getPaciente().getTelefonoParticular(),
						turno.getPaciente().getTelefonoOficina()),
				turno.getObraSocial() == null ? null : turno.getObraSocial().getNombre(),
				turno.getPlan() == null ? null : turno.getPlan().getNombre(),
				turno.getPaciente() == null ? null : turno.getPaciente().getNumeroAfiliado(),
				turno.getObservacion(),
				turno.getDadoModificadoPor(),
				turno.getFechaModificacion() == null ? null : turno.getFechaModificacion().format(FECHA_CORTA) + " "
						+ turno.getFechaModificacion().format(HORA_CORTA));
	}

	private Profesional profesionalSeleccionado(Long profesionalId) {
		if (profesionalId == null) {
			return null;
		}
		Profesional profesional = profesionalRepository.findById(profesionalId).orElse(null);
		return profesional;
	}

	private List<HorarioDiaResumen> resumenHorarios(Profesional profesional) {
		if (profesional == null) {
			return List.of();
		}
		return DIAS_ATENCION.stream()
				.map(dia -> {
					List<HorarioTramoResumen> tramos = profesional.getHorarios().stream()
							.filter(horario -> dia.equals(horario.getDia()))
							.filter(this::horarioCompleto)
							.sorted((a, b) -> a.getHoraDesde().compareTo(b.getHoraDesde()))
							.map(horario -> new HorarioTramoResumen(
									horario.getHoraDesde().format(HORA_CORTA),
									horario.getHoraHasta().format(HORA_CORTA)))
							.toList();
					boolean agendaOnline = profesional.getHorarios().stream()
							.filter(horario -> dia.equals(horario.getDia()))
							.filter(this::horarioCompleto)
							.anyMatch(ProfesionalHorario::isAgendaOnline);
					return new HorarioDiaResumen(dia, tramos, agendaOnline);
				})
				.toList();
	}

	private boolean horarioCompleto(ProfesionalHorario horario) {
		return horario.getHoraDesde() != null && horario.getHoraHasta() != null;
	}

	public record HorarioDiaResumen(String dia, List<HorarioTramoResumen> tramos, boolean agendaOnline) {
	}

	public record HorarioTramoResumen(String desde, String hasta) {
	}

	private List<DiaAgenda> construirDiasSemana(LocalDate dia) {
		LocalDate lunes = dia.with(DayOfWeek.MONDAY);
		return List.of(
				new DiaAgenda("L", lunes, dia.equals(lunes)),
				new DiaAgenda("M", lunes.plusDays(1), dia.equals(lunes.plusDays(1))),
				new DiaAgenda("M", lunes.plusDays(2), dia.equals(lunes.plusDays(2))),
				new DiaAgenda("J", lunes.plusDays(3), dia.equals(lunes.plusDays(3))),
				new DiaAgenda("V", lunes.plusDays(4), dia.equals(lunes.plusDays(4))),
				new DiaAgenda("S", lunes.plusDays(5), dia.equals(lunes.plusDays(5))),
				new DiaAgenda("D", lunes.plusDays(6), dia.equals(lunes.plusDays(6))));
	}

	@PostMapping("/turnos")
	public String guardarTurno(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dia,
			@RequestParam String hora, @RequestParam Long profesionalId,
			@RequestParam(required = false) Long pacienteId,
			@RequestParam(required = false) String pacienteNuevoNombre,
			@RequestParam(required = false) String pacienteNuevoDocumento,
			@RequestParam(required = false) String pacienteNuevoTelefono,
			@RequestParam(required = false) String pacienteNuevoEmail,
			@RequestParam(required = false) Long obraSocialId,
			@RequestParam(required = false) Long planId,
			@RequestParam(required = false) String observacion,
			@RequestParam(defaultValue = "false") boolean sobreturno,
			RedirectAttributes redirectAttributes) {
		try {
			String asignadoPor = usuarioService.nombreCompletoActual();
			Turno turno = new Turno();
			turno.setDia(dia);
			turno.setHora(LocalTime.parse(hora));
			turno.setProfesional(profesionalRepository.findById(profesionalId).orElseThrow());
			turno.setSobreturno(sobreturno);
			turno.setObservacion(observacion);
			turno.setDadoModificadoPor(asignadoPor);
			if (pacienteId != null) {
				turno.setPaciente(pacienteRepository.findById(pacienteId).orElseThrow());
			} else if (pacienteNuevoNombre != null && !pacienteNuevoNombre.isBlank()) {
				Paciente paciente = new Paciente();
				paciente.setNombreCompleto(pacienteNuevoNombre.trim());
				paciente.setTipoDocumento("DNI");
				paciente.setDocumento(pacienteNuevoDocumento);
				paciente.setTelefonoCelular(pacienteNuevoTelefono);
				paciente.setEmail(pacienteNuevoEmail);
				if (obraSocialId != null) {
					paciente.setObraSocial(obraSocialRepository.findById(obraSocialId).orElse(null));
				}
				if (planId != null) {
					paciente.setPlan(planRepository.findById(planId).orElse(null));
				}
				turno.setPaciente(paciente);
			}
			if (obraSocialId != null) {
				turno.setObraSocial(obraSocialRepository.findById(obraSocialId).orElse(null));
			} else if (turno.getPaciente() != null) {
				turno.setObraSocial(turno.getPaciente().getObraSocial());
			}
			if (planId != null) {
				turno.setPlan(planRepository.findById(planId).orElse(null));
			} else if (turno.getPaciente() != null) {
				turno.setPlan(turno.getPaciente().getPlan());
			}
			Turno guardado = turnoService.guardar(turno);
			if (guardado.getPaciente() != null) {
				turnoEmailService.notificarAsignacion(guardado, asignadoPor);
			}
			redirectAttributes.addFlashAttribute("ok", "Turno guardado.");
		} catch (RuntimeException ex) {
			redirectAttributes.addFlashAttribute("error", ex.getMessage());
		}
		return redirectTurnos(dia, profesionalId);
	}

	@PostMapping("/turnos/{id}/presentismo")
	public String cambiarPresentismo(@PathVariable Long id, @RequestParam Presentismo presentismo,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dia,
			@RequestParam(required = false) Long profesionalId) {
		turnoService.cambiarPresentismo(id, presentismo, usuarioService.nombreCompletoActual());
		return redirectTurnos(dia, profesionalId);
	}

	@PostMapping("/turnos/{id}/modificar")
	public String modificarTurno(@PathVariable Long id,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dia,
			@RequestParam String hora, @RequestParam Long profesionalId,
			@RequestParam(required = false) String observacion,
			RedirectAttributes redirectAttributes) {
		try {
			String modificadoPor = usuarioService.nombreCompletoActual();
			Turno turno = turnoService.obtener(id);
			turno.setDia(dia);
			turno.setHora(LocalTime.parse(hora));
			turno.setProfesional(profesionalRepository.findById(profesionalId).orElseThrow());
			turno.setObservacion(observacion);
			turno.setDadoModificadoPor(modificadoPor);
			Turno guardado = turnoService.guardar(turno);
			if (guardado.getPaciente() != null) {
				turnoEmailService.notificarAsignacion(guardado, modificadoPor);
			}
			redirectAttributes.addFlashAttribute("ok", "Turno modificado.");
		} catch (RuntimeException ex) {
			redirectAttributes.addFlashAttribute("error", ex.getMessage());
		}
		return redirectTurnos(dia, profesionalId);
	}

	@PostMapping("/turnos/{id}/anular")
	public String anular(@PathVariable Long id,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dia,
			@RequestParam(required = false) Long profesionalId) {
		String anuladoPor = usuarioService.nombreCompletoActual();
		Turno turno = turnoService.obtener(id);
		boolean estabaAsignado = turno.getPaciente() != null;
		TurnoEmailData emailData = TurnoEmailData.desde(turno, anuladoPor);
		turnoService.anular(id, anuladoPor);
		if (estabaAsignado) {
			turnoEmailService.notificarAnulacion(emailData);
		}
		return redirectTurnos(dia, profesionalId);
	}

	@PostMapping("/turnos/anular-horario")
	public String anularHorario(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dia,
			@RequestParam String hora, @RequestParam(required = false) Long profesionalId,
			RedirectAttributes redirectAttributes) {
		try {
			turnoService.anularHorario(dia, LocalTime.parse(hora), profesionalId, usuarioService.nombreCompletoActual());
		} catch (RuntimeException ex) {
			redirectAttributes.addFlashAttribute("error", ex.getMessage());
		}
		return redirectTurnos(dia, profesionalId);
	}

	@PostMapping("/turnos/{id}/liberar")
	public String liberar(@PathVariable Long id,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dia,
			@RequestParam(required = false) Long profesionalId) {
		turnoService.liberar(id);
		return redirectTurnos(dia, profesionalId);
	}

	@ModelAttribute("nuevoTurno")
	public Turno nuevoTurno() {
		return new Turno();
	}

	private String redirectTurnos(LocalDate dia, Long profesionalId) {
		String redirect = "redirect:/turnos?fecha=" + dia;
		return profesionalId == null ? redirect : redirect + "&profesionalId=" + profesionalId;
	}
}
