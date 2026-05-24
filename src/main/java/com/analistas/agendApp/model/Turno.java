package com.analistas.agendApp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "turnos")
@Getter
@Setter
@NoArgsConstructor
public class Turno {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private LocalDate dia;
	private LocalTime hora;

	@Enumerated(EnumType.STRING)
	private Presentismo presentismo = Presentismo.SIN_CONFIRMAR;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "paciente_id")
	private Paciente paciente;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "profesional_id")
	private Profesional profesional;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "obra_social_id")
	private ObraSocial obraSocial;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "plan_obra_social_id")
	private PlanObraSocial plan;

	@Column(length = 1000)
	private String observacion;

	private String dadoModificadoPor;
	private LocalDateTime fechaModificacion;
	private boolean sobreturno = false;

	@PrePersist
	@PreUpdate
	void touch() {
		fechaModificacion = LocalDateTime.now();
		if (presentismo == null || presentismo == Presentismo.LIMPIAR_ASISTENCIA) {
			presentismo = Presentismo.SIN_CONFIRMAR;
		}
	}
}
