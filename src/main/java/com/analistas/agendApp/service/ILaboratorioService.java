package com.analistas.agendApp.service;

import java.util.List;

import com.analistas.agendApp.model.Laboratorio;

public interface ILaboratorioService {
	List<Laboratorio> buscar(String q);
	Laboratorio obtener(Long id);
	Laboratorio guardar(Laboratorio laboratorio);
	void eliminar(Long id);
}
