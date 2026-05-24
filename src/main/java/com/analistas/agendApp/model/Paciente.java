package com.analistas.agendApp.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pacientes")
@Getter
@Setter
@NoArgsConstructor
public class Paciente {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String nombreCompleto;
	private String tipoDocumento;
	private String documento;
	private String historiaClinica;
	private LocalDate fechaNacimiento;
	private String sexo;
	private LocalDate fechaAlta;
	private LocalDate ultimaAtencion;
	private BigDecimal saldoAcreedor = BigDecimal.ZERO;
	private LocalDate fechaRecitacion;
	private String motivoRecitacion;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "obra_social_id")
	private ObraSocial obraSocial;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "plan_obra_social_id")
	private PlanObraSocial plan;

	private String numeroAfiliado;
	private String condicionAfiliatoria;
	private String telefonoCelular;
	private String telefonoParticular;
	private String telefonoOficina;
	private String email;
	private String domicilio;
	private String localidad;
	private String provincia;
	private String facebook;
	private String instagram;
	private String twitter;
	private String estadoCivil;
	private String ocupacion;
	private String cuitCuil;
	private String condicionFiscal;
	private Boolean hepatitis = false;
	private Boolean embarazo = false;
	private Boolean diabetes = false;
	private Boolean alergia = false;
	private Boolean cardiaco = false;
	private Boolean renal = false;
	private Boolean hiv = false;

	@Column(length = 1200)
	private String observaciones;

	@Column(length = 1200)
	private String alerta;

	@PrePersist
	void prePersist() {
		if (fechaAlta == null) {
			fechaAlta = LocalDate.now();
		}
	}
}
