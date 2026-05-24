package com.analistas.agendApp.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.analistas.agendApp.model.Proveedor;
import com.analistas.agendApp.repository.IProveedorRepository;
import com.analistas.agendApp.service.IProveedorService;

@Service
public class ProveedorServiceImpl implements IProveedorService {
	private final IProveedorRepository proveedorRepository;

	public ProveedorServiceImpl(IProveedorRepository proveedorRepository) {
		this.proveedorRepository = proveedorRepository;
	}

	@Override
	public List<Proveedor> buscar(String q) {
		return proveedorRepository.search(blankToNull(q));
	}

	@Override
	public Proveedor obtener(Long id) {
		return proveedorRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Proveedor inexistente"));
	}

	@Override
	public Proveedor guardar(Proveedor proveedor) {
		return proveedorRepository.save(proveedor);
	}

	@Override
	public void eliminar(Long id) {
		proveedorRepository.deleteById(id);
	}

	private String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
