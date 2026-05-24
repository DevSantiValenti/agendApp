package com.analistas.agendApp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.analistas.agendApp.model.Laboratorio;

public interface ILaboratorioRepository extends JpaRepository<Laboratorio, Long> {
	@Query("""
			select l from Laboratorio l
			where :q is null
			   or lower(coalesce(l.nombre, '')) like lower(concat('%', :q, '%'))
			   or lower(coalesce(l.codigo, '')) like lower(concat('%', :q, '%'))
			order by l.nombre
			""")
	List<Laboratorio> search(@Param("q") String q);
}
