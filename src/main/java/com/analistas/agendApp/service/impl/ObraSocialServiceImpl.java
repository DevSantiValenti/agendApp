package com.analistas.agendApp.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.analistas.agendApp.model.ObraSocial;
import com.analistas.agendApp.model.PlanObraSocial;
import com.analistas.agendApp.repository.IObraSocialRepository;
import com.analistas.agendApp.repository.IPlanObraSocialRepository;
import com.analistas.agendApp.service.IObraSocialService;

@Service
public class ObraSocialServiceImpl implements IObraSocialService {
	private final IObraSocialRepository obraSocialRepository;
	private final IPlanObraSocialRepository planRepository;

	public ObraSocialServiceImpl(IObraSocialRepository obraSocialRepository, IPlanObraSocialRepository planRepository) {
		this.obraSocialRepository = obraSocialRepository;
		this.planRepository = planRepository;
	}

	@Override
	public List<ObraSocial> buscar(String q) {
		return obraSocialRepository.search(blankToNull(q));
	}

	@Override
	public List<ObraSocial> habilitadas() {
		return obraSocialRepository.findByHabilitadoTrueOrderByNombreAsc();
	}

	@Override
	public ObraSocial obtener(Long id) {
		return obraSocialRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Obra social inexistente"));
	}

	@Override
	public ObraSocial guardar(ObraSocial obraSocial) {
		return obraSocialRepository.save(obraSocial);
	}

	@Override
	public void eliminar(Long id) {
		obraSocialRepository.deleteById(id);
	}

	@Override
	public PlanObraSocial agregarPlan(Long obraSocialId, PlanObraSocial plan) {
		ObraSocial obraSocial = obtener(obraSocialId);
		plan.setObraSocial(obraSocial);
		return planRepository.save(plan);
	}

	@Override
	public void eliminarPlan(Long planId) {
		planRepository.deleteById(planId);
	}

	private String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
