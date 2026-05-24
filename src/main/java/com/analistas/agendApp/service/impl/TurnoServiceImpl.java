package com.analistas.agendApp.service.impl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.analistas.agendApp.dto.TurnoSlot;
import com.analistas.agendApp.model.Presentismo;
import com.analistas.agendApp.model.Turno;
import com.analistas.agendApp.repository.ITurnoRepository;
import com.analistas.agendApp.service.ITurnoService;

@Service
public class TurnoServiceImpl implements ITurnoService {
	private static final LocalTime HORA_INICIO = LocalTime.of(8, 0);
	private static final LocalTime HORA_FIN = LocalTime.of(20, 0);
	private static final String NO_CITAR = "NO CITAR ESTE TURNO";

	private final ITurnoRepository turnoRepository;

	public TurnoServiceImpl(ITurnoRepository turnoRepository) {
		this.turnoRepository = turnoRepository;
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
		for (LocalTime hora = HORA_INICIO; !hora.isAfter(HORA_FIN); hora = hora.plusMinutes(30)) {
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
		validarColision(turno);
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
}
