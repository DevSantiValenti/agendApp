package com.analistas.agendApp.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.analistas.agendApp.model.Profesional;
import com.analistas.agendApp.repository.IProfesionalRepository;
import com.analistas.agendApp.service.IProfesionalService;

@Service
public class ProfesionalServiceImpl implements IProfesionalService {
	private final IProfesionalRepository profesionalRepository;

	public ProfesionalServiceImpl(IProfesionalRepository profesionalRepository) {
		this.profesionalRepository = profesionalRepository;
	}

	@Override
	public List<Profesional> buscar(String q) {
		return profesionalRepository.search(blankToNull(q));
	}

	@Override
	public List<Profesional> habilitados() {
		return profesionalRepository.findByHabilitadoTrueOrderByApellidoNombreAsc();
	}

	@Override
	public List<Profesional> habilitadosPorEspecialidad(Long especialidadId) {
		return profesionalRepository.findHabilitadosByEspecialidad(especialidadId);
	}

	@Override
	public Profesional obtener(Long id) {
		return profesionalRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Profesional inexistente"));
	}

	@Override
	public Profesional guardar(Profesional profesional) {
		return profesionalRepository.save(profesional);
	}

	@Override
	public void eliminar(Long id) {
		profesionalRepository.deleteById(id);
	}

	private String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
