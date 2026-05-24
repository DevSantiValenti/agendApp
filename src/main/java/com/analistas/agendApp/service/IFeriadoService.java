package com.analistas.agendApp.service;

import java.util.List;

import com.analistas.agendApp.model.Feriado;

public interface IFeriadoService {
	List<Feriado> listar();
	Feriado obtener(Long id);
	Feriado guardar(Feriado feriado);
	void eliminar(Long id);
}
