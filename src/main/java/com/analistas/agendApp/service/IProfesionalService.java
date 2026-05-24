package com.analistas.agendApp.service;

import java.util.List;

import com.analistas.agendApp.model.Profesional;

public interface IProfesionalService {
	List<Profesional> buscar(String q);
	List<Profesional> habilitados();
	List<Profesional> habilitadosPorEspecialidad(Long especialidadId);
	Profesional obtener(Long id);
	Profesional guardar(Profesional profesional);
	void eliminar(Long id);
}
