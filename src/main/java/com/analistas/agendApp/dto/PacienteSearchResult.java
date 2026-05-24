package com.analistas.agendApp.dto;

public record PacienteSearchResult(
		Long id,
		String nombreCompleto,
		String documento,
		String historiaClinica,
		String obraSocial,
		String plan,
		java.time.LocalDate fechaNacimiento,
		String numeroAfiliado,
		String domicilio) {
}
