package com.analistas.agendApp.dto;

import java.time.LocalTime;

import com.analistas.agendApp.model.Turno;

public record TurnoSlot(LocalTime hora, Turno turno) {
	public boolean ocupado() {
		return turno != null;
	}
}
