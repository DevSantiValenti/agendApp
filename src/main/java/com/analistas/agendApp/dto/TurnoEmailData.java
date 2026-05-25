package com.analistas.agendApp.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.analistas.agendApp.model.Turno;

public record TurnoEmailData(
		String profesionalEmail,
		String profesionalNombre,
		String pacienteEmail,
		LocalDate dia,
		LocalTime hora,
		String actor,
		String pacienteNombre,
		String observacion) {

	public static TurnoEmailData desde(Turno turno, String actor) {
		return new TurnoEmailData(
				turno.getProfesional() == null ? null : turno.getProfesional().getEmail(),
				turno.getProfesional() == null ? null : turno.getProfesional().getApellidoNombre(),
				turno.getPaciente() == null ? null : turno.getPaciente().getEmail(),
				turno.getDia(),
				turno.getHora(),
				actor,
				turno.getPaciente() == null ? null : turno.getPaciente().getNombreCompleto(),
				turno.getObservacion());
	}

	public boolean tieneDestinatario() {
		return profesionalEmail != null && !profesionalEmail.isBlank();
	}

	public boolean tienePacienteEmail() {
		return pacienteEmail != null && !pacienteEmail.isBlank();
	}
}
