package com.analistas.agendApp.model;

public enum Presentismo {
	PRESENTE("Presente", "presente", "PRE"),
	AUSENTE_CON_AVISO("Ausente con aviso", "ausente-aviso", "ACA"),
	AUSENTE_SIN_AVISO("Ausente sin aviso", "ausente-sin-aviso", "ASA"),
	ATENDIDO("Atendido", "atendido", "ATE"),
	CONFIRMADO("Confirmado", "confirmado", "CON"),
	CANCELADO("Cancelado", "cancelado", "CAN"),
	SIN_CONFIRMAR("Sin confirmar", "sin-confirmar", "SIN"),
	LIMPIAR_ASISTENCIA("Limpiar Asistencia", "limpiar-asistencia", "LIM");

	private final String etiqueta;
	private final String cssClass;
	private final String codigo;

	Presentismo(String etiqueta, String cssClass, String codigo) {
		this.etiqueta = etiqueta;
		this.cssClass = cssClass;
		this.codigo = codigo;
	}

	public String getEtiqueta() {
		return etiqueta;
	}

	public String getCssClass() {
		return cssClass;
	}

	public String getCodigo() {
		return codigo;
	}
}
