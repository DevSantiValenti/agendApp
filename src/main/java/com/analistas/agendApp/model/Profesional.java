package com.analistas.agendApp.model;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "profesionales")
@Getter
@Setter
@NoArgsConstructor
public class Profesional {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 30)
	private String codigo;

	@Column(length = 20)
	private String trato;

	@Column(nullable = false, length = 150)
	private String apellidoNombre;

	private String domicilio;
	private String localidad;
	private String provincia;
	private String telefono;
	private String email;
	private String cuit;
	private String condicionFiscal;
	private String profesion;
	private String tipoMatricula;
	private String jurisdiccion;
	private String matricula;

	@Column(length = 1000)
	private String observaciones;

	private String codigoLiquidacionOsde;
	private boolean habilitado = true;
	private boolean agendaGuardia = false;

	@Column(length = 1200)
	private String configuracionFirma;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
			name = "profesionales_especialidades",
			joinColumns = @JoinColumn(name = "profesional_id"),
			inverseJoinColumns = @JoinColumn(name = "especialidad_id"))
	private Set<Especialidad> especialidades = new HashSet<>();

	@OneToMany(mappedBy = "profesional", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ProfesionalHorario> horarios = new ArrayList<>();
}
