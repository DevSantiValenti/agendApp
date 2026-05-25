package com.analistas.agendApp.service.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.analistas.agendApp.dto.TurnoSlot;
import com.analistas.agendApp.model.Presentismo;
import com.analistas.agendApp.model.Profesional;
import com.analistas.agendApp.model.ProfesionalHorario;
import com.analistas.agendApp.model.Turno;
import com.analistas.agendApp.repository.IPacienteRepository;
import com.analistas.agendApp.repository.IProfesionalRepository;
import com.analistas.agendApp.repository.ITurnoRepository;
import com.analistas.agendApp.service.ITurnoService;

@Service
public class TurnoServiceImpl implements ITurnoService {
	private static final LocalTime HORA_INICIO = LocalTime.of(8, 0);
	private static final LocalTime HORA_FIN = LocalTime.of(20, 0);
	private static final int DURACION_SLOT_MINUTOS = 30;
	private static final String NO_CITAR = "NO CITAR ESTE TURNO";

	private final ITurnoRepository turnoRepository;
	private final IProfesionalRepository profesionalRepository;
	private final IPacienteRepository pacienteRepository;

	public TurnoServiceImpl(ITurnoRepository turnoRepository, IProfesionalRepository profesionalRepository,
			IPacienteRepository pacienteRepository) {
		this.turnoRepository = turnoRepository;
		this.profesionalRepository = profesionalRepository;
		this.pacienteRepository = pacienteRepository;
	}

	@Override
	public List<Turno> buscarPorDia(LocalDate dia, Long profesionalId) {
		if (profesionalId == null) {
			return turnoRepository.findByDiaOrderByHoraAsc(dia);
		}
		return turnoRepository.findByDiaAndProfesionalIdOrderByHoraAsc(dia, profesionalId);
	}

	@Override
	public List<TurnoSlot> construirAgenda(LocalDate dia, Long profesionalId) {
		Map<LocalTime, Turno> turnosPorHora = new LinkedHashMap<>();
		buscarPorDia(dia, profesionalId).forEach(turno -> turnosPorHora.putIfAbsent(turno.getHora(), turno));

		List<TurnoSlot> slots = new ArrayList<>();
		for (LocalTime hora : horasAgenda(dia, profesionalId)) {
			slots.add(new TurnoSlot(hora, turnosPorHora.remove(hora)));
		}
		turnosPorHora.values().forEach(turno -> slots.add(new TurnoSlot(turno.getHora(), turno)));
		slots.sort((a, b) -> a.hora().compareTo(b.hora()));
		return slots;
	}

	@Override
	public Turno obtener(Long id) {
		return turnoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Turno inexistente"));
	}

	@Override
	@Transactional
	public Turno guardar(Turno turno) {
		validarHorarioProfesional(turno);
		validarColision(turno);
		if (turno.getPaciente() != null && turno.getPaciente().getId() == null) {
			turno.setPaciente(pacienteRepository.save(turno.getPaciente()));
		}
		return turnoRepository.save(turno);
	}

	@Override
	@Transactional
	public Turno cambiarPresentismo(Long id, Presentismo presentismo, String modificadoPor) {
		Turno turno = obtener(id);
		turno.setPresentismo(presentismo == Presentismo.LIMPIAR_ASISTENCIA ? Presentismo.SIN_CONFIRMAR : presentismo);
		turno.setDadoModificadoPor(modificadoPor);
		return turnoRepository.save(turno);
	}

	@Override
	@Transactional
	public void anular(Long id, String modificadoPor) {
		turnoRepository.save(marcarNoCitar(obtener(id), modificadoPor));
	}

	@Override
	@Transactional
	public void liberar(Long id) {
		turnoRepository.delete(obtener(id));
	}

	@Override
	@Transactional
	public Turno anularHorario(LocalDate dia, LocalTime hora, Long profesionalId, String modificadoPor) {
		if (profesionalId == null) {
			throw new IllegalArgumentException("Seleccioná un profesional para anular el horario.");
		}
		Turno turno = turnoRepository.findFirstByDiaAndHoraAndProfesionalIdAndSobreturnoFalse(dia, hora, profesionalId)
				.orElseGet(Turno::new);
		turno.setDia(dia);
		turno.setHora(hora);
		turno.setProfesional(new com.analistas.agendApp.model.Profesional());
		turno.getProfesional().setId(profesionalId);
		turno.setSobreturno(false);
		return turnoRepository.save(marcarNoCitar(turno, modificadoPor));
	}

	private Turno marcarNoCitar(Turno turno, String modificadoPor) {
		turno.setPaciente(null);
		turno.setObraSocial(null);
		turno.setPlan(null);
		turno.setPresentismo(Presentismo.CANCELADO);
		turno.setObservacion(NO_CITAR);
		turno.setDadoModificadoPor(modificadoPor);
		return turno;
	}

	private void validarColision(Turno turno) {
		if (turno.isSobreturno() || turno.getProfesional() == null || turno.getProfesional().getId() == null) {
			return;
		}
		turnoRepository.findFirstByDiaAndHoraAndProfesionalIdAndSobreturnoFalse(
				turno.getDia(), turno.getHora(), turno.getProfesional().getId())
				.filter(existing -> turno.getId() == null || !existing.getId().equals(turno.getId()))
				.ifPresent(existing -> {
					throw new IllegalArgumentException("Ya existe un turno para ese profesional, dia y hora.");
				});
	}

	private void validarHorarioProfesional(Turno turno) {
		if (turno.getProfesional() == null || turno.getProfesional().getId() == null || turno.getDia() == null
				|| turno.getHora() == null) {
			throw new IllegalArgumentException("Seleccioná profesional, día y hora para guardar el turno.");
		}
		Profesional profesional = profesionalRepository.findById(turno.getProfesional().getId())
				.orElseThrow(() -> new IllegalArgumentException("Seleccioná un profesional válido."));
		String dia = nombreDia(turno.getDia().getDayOfWeek());
		List<ProfesionalHorario> horariosDelDia = profesional.getHorarios().stream()
				.filter(horario -> dia.equals(horario.getDia()))
				.toList();
		if (horariosDelDia.isEmpty()) {
			throw new IllegalArgumentException("El profesional no tiene horarios configurados para " + dia.toLowerCase() + ".");
		}
		boolean horarioDisponible = horariosDelDia.stream().anyMatch(horario -> estaDentroDelHorario(turno.getHora(), horario));
		if (!horarioDisponible) {
			throw new IllegalArgumentException("El profesional no atiende en ese horario.");
		}
	}

	private boolean estaDentroDelHorario(LocalTime hora, ProfesionalHorario horario) {
		LocalTime desde = horario.getHoraDesde();
		LocalTime hasta = horario.getHoraHasta();
		return desde != null && hasta != null && !hora.isBefore(desde)
				&& !hora.plusMinutes(DURACION_SLOT_MINUTOS).isAfter(hasta);
	}

	private List<LocalTime> horasAgenda(LocalDate dia, Long profesionalId) {
		if (profesionalId == null) {
			List<LocalTime> horas = new ArrayList<>();
			for (LocalTime hora = HORA_INICIO; !hora.isAfter(HORA_FIN); hora = hora.plusMinutes(DURACION_SLOT_MINUTOS)) {
				horas.add(hora);
			}
			return horas;
		}
		Profesional profesional = profesionalRepository.findById(profesionalId).orElse(null);
		if (profesional == null) {
			return List.of();
		}
		String diaNombre = nombreDia(dia.getDayOfWeek());
		return profesional.getHorarios().stream()
				.filter(horario -> diaNombre.equals(horario.getDia()))
				.filter(horario -> horario.getHoraDesde() != null && horario.getHoraHasta() != null)
				.flatMap(horario -> horasDelTramo(horario).stream())
				.distinct()
				.sorted()
				.collect(Collectors.toList());
	}

	private List<LocalTime> horasDelTramo(ProfesionalHorario horario) {
		List<LocalTime> horas = new ArrayList<>();
		for (LocalTime hora = horario.getHoraDesde();
				!hora.plusMinutes(DURACION_SLOT_MINUTOS).isAfter(horario.getHoraHasta());
				hora = hora.plusMinutes(DURACION_SLOT_MINUTOS)) {
			horas.add(hora);
		}
		return horas;
	}

	private String nombreDia(DayOfWeek dayOfWeek) {
		return switch (dayOfWeek) {
			case MONDAY -> "Lunes";
			case TUESDAY -> "Martes";
			case WEDNESDAY -> "Miercoles";
			case THURSDAY -> "Jueves";
			case FRIDAY -> "Viernes";
			case SATURDAY -> "Sabado";
			case SUNDAY -> "Domingo";
		};
	}
}
