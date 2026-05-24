package com.analistas.agendApp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.analistas.agendApp.model.Feriado;

public interface IFeriadoRepository extends JpaRepository<Feriado, Long> {
	List<Feriado> findAllByOrderByFechaDesdeDesc();
}
