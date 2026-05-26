package com.analistas.agendApp.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.analistas.agendApp.model.Paciente;
import com.analistas.agendApp.repository.IPacienteRepository;
import com.analistas.agendApp.repository.ITurnoRepository;
import com.analistas.agendApp.service.IPacienteService;

@Service
public class PacienteServiceImpl implements IPacienteService {
	private final IPacienteRepository pacienteRepository;
	private final ITurnoRepository turnoRepository;

	public PacienteServiceImpl(IPacienteRepository pacienteRepository, ITurnoRepository turnoRepository) {
		this.pacienteRepository = pacienteRepository;
		this.turnoRepository = turnoRepository;
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
	@Transactional
	public void eliminar(Long id) {
		Paciente paciente = obtener(id);
		turnoRepository.deleteByPacienteId(id);
		pacienteRepository.delete(paciente);
	}

	private String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
