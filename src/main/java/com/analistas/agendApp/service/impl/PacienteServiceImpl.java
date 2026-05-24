package com.analistas.agendApp.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.analistas.agendApp.model.Paciente;
import com.analistas.agendApp.repository.IPacienteRepository;
import com.analistas.agendApp.service.IPacienteService;

@Service
public class PacienteServiceImpl implements IPacienteService {
	private final IPacienteRepository pacienteRepository;

	public PacienteServiceImpl(IPacienteRepository pacienteRepository) {
		this.pacienteRepository = pacienteRepository;
	}

	@Override
	public List<Paciente> buscar(String q) {
		return pacienteRepository.search(blankToNull(q));
	}

	@Override
	public Paciente obtener(Long id) {
		return pacienteRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Paciente inexistente"));
	}

	@Override
	public Paciente guardar(Paciente paciente) {
		return pacienteRepository.save(paciente);
	}

	@Override
	public void eliminar(Long id) {
		pacienteRepository.deleteById(id);
	}

	private String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
