package com.analistas.agendApp.dto;

import java.time.LocalDate;

public record DiaAgenda(String etiqueta, LocalDate fecha, boolean seleccionado) {
}
