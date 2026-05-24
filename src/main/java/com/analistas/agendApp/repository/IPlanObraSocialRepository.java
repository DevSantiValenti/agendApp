package com.analistas.agendApp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.analistas.agendApp.model.PlanObraSocial;

public interface IPlanObraSocialRepository extends JpaRepository<PlanObraSocial, Long> {
	List<PlanObraSocial> findByObraSocialIdOrderByNombreAsc(Long obraSocialId);
}
