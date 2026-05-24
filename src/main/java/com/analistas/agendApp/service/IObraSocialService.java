package com.analistas.agendApp.service;

import java.util.List;

import com.analistas.agendApp.model.ObraSocial;
import com.analistas.agendApp.model.PlanObraSocial;

public interface IObraSocialService {
	List<ObraSocial> buscar(String q);
	List<ObraSocial> habilitadas();
	ObraSocial obtener(Long id);
	ObraSocial guardar(ObraSocial obraSocial);
	void eliminar(Long id);
	PlanObraSocial agregarPlan(Long obraSocialId, PlanObraSocial plan);
	void eliminarPlan(Long planId);
}
