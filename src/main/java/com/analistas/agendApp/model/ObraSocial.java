package com.analistas.agendApp.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "obras_sociales")
@Getter
@Setter
@NoArgsConstructor
public class ObraSocial {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 30)
	private String codigo;

	@Column(nullable = false, length = 150)
	private String nombre;

	private String direccion;
	private String telefono;
	private String email;
	private String cuit;

	@Column(length = 1000)
	private String observaciones;

	private boolean habilitado = true;

	@OneToMany(mappedBy = "obraSocial", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<PlanObraSocial> planes = new ArrayList<>();
}
