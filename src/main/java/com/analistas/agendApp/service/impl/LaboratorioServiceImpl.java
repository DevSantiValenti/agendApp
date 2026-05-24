package com.analistas.agendApp.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.analistas.agendApp.model.Laboratorio;
import com.analistas.agendApp.repository.ILaboratorioRepository;
import com.analistas.agendApp.service.ILaboratorioService;

@Service
public class LaboratorioServiceImpl implements ILaboratorioService {
	private final ILaboratorioRepository laboratorioRepository;

	public LaboratorioServiceImpl(ILaboratorioRepository laboratorioRepository) {
		this.laboratorioRepository = laboratorioRepository;
	}

	@Override
	public List<Laboratorio> buscar(String q) {
		return laboratorioRepository.search(blankToNull(q));
	}

	@Override
	public Laboratorio obtener(Long id) {
		return laboratorioRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Laboratorio inexistente"));
	}

	@Override
	public Laboratorio guardar(Laboratorio laboratorio) {
		return laboratorioRepository.save(laboratorio);
	}

	@Override
	public void eliminar(Long id) {
		laboratorioRepository.deleteById(id);
	}

	private String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
