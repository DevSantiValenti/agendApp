package com.analistas.agendApp.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.analistas.agendApp.model.Feriado;
import com.analistas.agendApp.repository.IFeriadoRepository;
import com.analistas.agendApp.service.IFeriadoService;

@Service
public class FeriadoServiceImpl implements IFeriadoService {
	private final IFeriadoRepository feriadoRepository;

	public FeriadoServiceImpl(IFeriadoRepository feriadoRepository) {
		this.feriadoRepository = feriadoRepository;
	}

	@Override
	public List<Feriado> listar() {
		return feriadoRepository.findAllByOrderByFechaDesdeDesc();
	}

	@Override
	public Feriado obtener(Long id) {
		return feriadoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Feriado inexistente"));
	}

	@Override
	public Feriado guardar(Feriado feriado) {
		return feriadoRepository.save(feriado);
	}

	@Override
	public void eliminar(Long id) {
		feriadoRepository.deleteById(id);
	}
}
