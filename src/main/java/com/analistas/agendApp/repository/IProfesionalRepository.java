package com.analistas.agendApp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.analistas.agendApp.model.Profesional;

public interface IProfesionalRepository extends JpaRepository<Profesional, Long> {
	@EntityGraph(attributePaths = {"especialidades", "horarios"})
	List<Profesional> findByHabilitadoTrueOrderByApellidoNombreAsc();

	@Query("""
			select distinct p from Profesional p
			left join p.especialidades e
			where :q is null
			   or lower(coalesce(p.apellidoNombre, '')) like lower(concat('%', :q, '%'))
			   or lower(coalesce(p.codigo, '')) like lower(concat('%', :q, '%'))
			   or lower(coalesce(p.profesion, '')) like lower(concat('%', :q, '%'))
			order by p.apellidoNombre
			""")
	@EntityGraph(attributePaths = {"especialidades", "horarios"})
	List<Profesional> search(@Param("q") String q);

	@Query("""
			select distinct p from Profesional p
			left join p.especialidades e
			where p.habilitado = true
			  and (:especialidadId is null or e.id = :especialidadId)
			order by p.apellidoNombre
			""")
	@EntityGraph(attributePaths = {"especialidades", "horarios"})
	List<Profesional> findHabilitadosByEspecialidad(@Param("especialidadId") Long especialidadId);

	@Override
	@EntityGraph(attributePaths = {"especialidades", "horarios"})
	java.util.Optional<Profesional> findById(Long id);
}
