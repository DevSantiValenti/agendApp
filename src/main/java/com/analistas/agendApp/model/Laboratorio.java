package com.analistas.agendApp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "laboratorios")
@Getter
@Setter
@NoArgsConstructor
public class Laboratorio {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 30)
	private String codigo;

	@Column(nullable = false, length = 150)
	private String nombre;

	private String contacto;
	private String telefono;
	private String email;
	private String direccion;

	@Column(length = 1000)
	private String observaciones;

	private boolean habilitado = true;
}
