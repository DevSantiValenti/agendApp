package com.analistas.agendApp.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.analistas.agendApp.dto.TurnoSlot;
import com.analistas.agendApp.model.Presentismo;
import com.analistas.agendApp.model.Turno;

public interface ITurnoService {
	List<Turno> buscarPorDia(LocalDate dia, Long profesionalId);
	List<TurnoSlot> construirAgenda(LocalDate dia, Long profesionalId);
	Turno obtener(Long id);
	Turno guardar(Turno turno);
	Turno cambiarPresentismo(Long id, Presentismo presentismo, String modificadoPor);
	void anular(Long id, String modificadoPor);
	void liberar(Long id);
	Turno anularHorario(LocalDate dia, LocalTime hora, Long profesionalId, String modificadoPor);
}
