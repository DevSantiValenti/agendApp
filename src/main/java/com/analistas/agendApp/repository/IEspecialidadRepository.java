package com.analistas.agendApp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.analistas.agendApp.model.Especialidad;

public interface IEspecialidadRepository extends JpaRepository<Especialidad, Long> {
	List<Especialidad> findAllByOrderByNombreAsc();

	@Query("""
			select e from Especialidad e
			where :q is null
			   or lower(coalesce(e.nombre, '')) like lower(concat('%', :q, '%'))
			   or lower(coalesce(e.codigo, '')) like lower(concat('%', :q, '%'))
			order by e.nombre
			""")
	List<Especialidad> search(@Param("q") String q);
}
