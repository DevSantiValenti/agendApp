package com.analistas.agendApp.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.analistas.agendApp.model.Especialidad;
import com.analistas.agendApp.repository.IEspecialidadRepository;
import com.analistas.agendApp.service.IEspecialidadService;

@Service
public class EspecialidadServiceImpl implements IEspecialidadService {
	private final IEspecialidadRepository especialidadRepository;

	public EspecialidadServiceImpl(IEspecialidadRepository especialidadRepository) {
		this.especialidadRepository = especialidadRepository;
	}

	@Override
	public List<Especialidad> buscar(String q) {
		return especialidadRepository.search(blankToNull(q));
	}

	@Override
	public List<Especialidad> listar() {
		return especialidadRepository.findAllByOrderByNombreAsc();
	}

	@Override
	public Especialidad obtener(Long id) {
		return especialidadRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Especialidad inexistente"));
	}

	@Override
	public Especialidad guardar(Especialidad especialidad) {
		return especialidadRepository.save(especialidad);
	}

	@Override
	public void eliminar(Long id) {
		especialidadRepository.deleteById(id);
	}

	private String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
