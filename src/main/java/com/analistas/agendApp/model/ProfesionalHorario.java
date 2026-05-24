package com.analistas.agendApp.model;

import java.time.LocalTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "profesionales_horarios")
@Getter
@Setter
@NoArgsConstructor
public class ProfesionalHorario {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String dia;
	private LocalTime horaDesde;
	private LocalTime horaHasta;
	private boolean agendaOnline = false;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "profesional_id")
	private Profesional profesional;
}
