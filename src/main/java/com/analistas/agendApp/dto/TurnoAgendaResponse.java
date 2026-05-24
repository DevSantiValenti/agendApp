package com.analistas.agendApp.dto;

import java.time.LocalDate;
import java.util.List;

public record TurnoAgendaResponse(
		LocalDate fecha,
		String fechaTexto,
		List<DiaAgenda> diasSemana,
		List<Slot> slots) {

	public record Slot(String hora, TurnoInfo turno) {
	}

	public record TurnoInfo(
			Long id,
			String presentismo,
			String presentismoEtiqueta,
			String presentismoCssClass,
			String presentismoCodigo,
			Long profesionalId,
			String profesional,
			PacienteInfo paciente,
			String obraSocial,
			String plan,
			String numeroAfiliado,
			String observacion,
			String dadoModificadoPor,
			String fechaModificacion) {
	}

	public record PacienteInfo(
			Long id,
			String nombreCompleto,
			String historiaClinica,
			String telefonoCelular,
			String telefonoParticular,
			String telefonoOficina) {
	}
}
