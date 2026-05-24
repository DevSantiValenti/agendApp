package com.analistas.agendApp.service;

import java.util.List;

import com.analistas.agendApp.model.Especialidad;

public interface IEspecialidadService {
	List<Especialidad> buscar(String q);
	List<Especialidad> listar();
	Especialidad obtener(Long id);
	Especialidad guardar(Especialidad especialidad);
	void eliminar(Long id);
}
