package com.analistas.agendApp.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import com.analistas.agendApp.model.Turno;

public interface ITurnoRepository extends JpaRepository<Turno, Long> {
	@EntityGraph(attributePaths = {"paciente", "profesional", "obraSocial", "plan"})
	List<Turno> findByDiaOrderByHoraAsc(LocalDate dia);

	@EntityGraph(attributePaths = {"paciente", "profesional", "obraSocial", "plan"})
	List<Turno> findByDiaAndProfesionalIdOrderByHoraAsc(LocalDate dia, Long profesionalId);

	Optional<Turno> findFirstByDiaAndHoraAndProfesionalIdAndSobreturnoFalse(LocalDate dia, LocalTime hora, Long profesionalId);

	long countByPacienteId(Long pacienteId);

	long countByProfesionalId(Long profesionalId);

	long deleteByPacienteId(Long pacienteId);

	long deleteByProfesionalId(Long profesionalId);
}
