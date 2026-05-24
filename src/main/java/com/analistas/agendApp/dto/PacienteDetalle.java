package com.analistas.agendApp.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PacienteDetalle(
		Long id,
		String nombreCompleto,
		String tipoDocumento,
		String documento,
		String historiaClinica,
		LocalDate fechaNacimiento,
		LocalDate fechaAlta,
		LocalDate ultimaAtencion,
		BigDecimal saldoAcreedor,
		LocalDate fechaRecitacion,
		String motivoRecitacion,
		String obraSocial,
		String plan,
		String numeroAfiliado,
		String condicionAfiliatoria,
		String telefonoCelular,
		String telefonoParticular,
		String telefonoOficina,
		String email,
		String domicilio,
		String localidad,
		String provincia,
		String cuitCuil,
		String condicionFiscal,
		Boolean hepatitis,
		Boolean embarazo,
		Boolean diabetes,
		Boolean alergia,
		Boolean cardiaco,
		Boolean renal,
		Boolean hiv,
		String observaciones,
		String alerta) {
}
