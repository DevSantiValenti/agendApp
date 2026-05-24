package com.analistas.agendApp.service;

import java.util.List;

import com.analistas.agendApp.model.Usuario;

public interface IUsuarioService {
	List<Usuario> listar();
	Usuario buscarPorId(Long id);
	Usuario guardar(Usuario usuario);
	Usuario actualizar(Long id, Usuario usuario);
	void eliminar(Long id);
	String nombreCompletoActual();
}
