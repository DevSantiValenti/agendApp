package com.analistas.agendApp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.analistas.agendApp.model.Proveedor;

public interface IProveedorRepository extends JpaRepository<Proveedor, Long> {
	@Query("""
			select p from Proveedor p
			where :q is null
			   or lower(coalesce(p.nombre, '')) like lower(concat('%', :q, '%'))
			   or lower(coalesce(p.cuit, '')) like lower(concat('%', :q, '%'))
			order by p.nombre
			""")
	List<Proveedor> search(@Param("q") String q);
}
