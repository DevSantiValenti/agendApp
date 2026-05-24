package com.analistas.agendApp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.analistas.agendApp.model.ObraSocial;

public interface IObraSocialRepository extends JpaRepository<ObraSocial, Long> {
	List<ObraSocial> findByHabilitadoTrueOrderByNombreAsc();

	@Query("""
			select o from ObraSocial o
			where :q is null
			   or lower(coalesce(o.nombre, '')) like lower(concat('%', :q, '%'))
			   or lower(coalesce(o.codigo, '')) like lower(concat('%', :q, '%'))
			order by o.nombre
			""")
	@EntityGraph(attributePaths = "planes")
	List<ObraSocial> search(@Param("q") String q);
}
