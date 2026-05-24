package com.analistas.agendApp.service;

import java.util.List;

import com.analistas.agendApp.model.Paciente;

public interface IPacienteService {
	List<Paciente> buscar(String q);
	Paciente obtener(Long id);
	Paciente guardar(Paciente paciente);
	void eliminar(Long id);
}
