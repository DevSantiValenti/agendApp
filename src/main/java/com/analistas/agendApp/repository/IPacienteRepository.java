package com.analistas.agendApp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.analistas.agendApp.model.Paciente;

public interface IPacienteRepository extends JpaRepository<Paciente, Long> {
	@Query("""
			select p from Paciente p
			where :q is null
			   or lower(coalesce(p.nombreCompleto, '')) like lower(concat('%', :q, '%'))
			   or lower(coalesce(p.documento, '')) like lower(concat('%', :q, '%'))
			   or lower(coalesce(p.historiaClinica, '')) like lower(concat('%', :q, '%'))
			order by p.nombreCompleto
			""")
	@EntityGraph(attributePaths = {"obraSocial", "plan"})
	List<Paciente> search(@Param("q") String q);

	@Override
	@EntityGraph(attributePaths = {"obraSocial", "plan"})
	java.util.Optional<Paciente> findById(Long id);
}
