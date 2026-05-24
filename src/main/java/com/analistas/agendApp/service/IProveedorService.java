package com.analistas.agendApp.service;

import java.util.List;

import com.analistas.agendApp.model.Proveedor;

public interface IProveedorService {
	List<Proveedor> buscar(String q);
	Proveedor obtener(Long id);
	Proveedor guardar(Proveedor proveedor);
	void eliminar(Long id);
}
