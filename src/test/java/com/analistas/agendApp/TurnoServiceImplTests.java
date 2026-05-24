package com.analistas.agendApp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.analistas.agendApp.model.Profesional;
import com.analistas.agendApp.model.Presentismo;
import com.analistas.agendApp.model.Turno;
import com.analistas.agendApp.repository.IProfesionalRepository;
import com.analistas.agendApp.service.ITurnoService;

@SpringBootTest
@Transactional
class TurnoServiceImplTests {
	@Autowired
	private ITurnoService turnoService;

	@Autowired
	private IProfesionalRepository profesionalRepository;

	@Test
	void rechazaTurnoDuplicadoParaMismoProfesionalDiaYHora() {
		Profesional profesional = profesionalRepository.save(profesional("BARRIOS DALMA"));
		LocalDate dia = LocalDate.of(2026, 5, 19);
		LocalTime hora = LocalTime.of(17, 30);

		turnoService.guardar(turno(profesional, dia, hora, false));

		assertThatThrownBy(() -> turnoService.guardar(turno(profesional, dia, hora, false)))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Ya existe un turno");
	}

	@Test
	void permiteSobreturnoEnMismoProfesionalDiaYHora() {
		Profesional profesional = profesionalRepository.save(profesional("SOLER PEDRO"));
		LocalDate dia = LocalDate.of(2026, 5, 19);
		LocalTime hora = LocalTime.of(18, 0);

		turnoService.guardar(turno(profesional, dia, hora, false));
		Turno sobreturno = turnoService.guardar(turno(profesional, dia, hora, true));

		assertThat(sobreturno.getId()).isNotNull();
		assertThat(sobreturno.isSobreturno()).isTrue();
	}

	@Test
	void agendaComienzaALasOcho() {
		assertThat(turnoService.construirAgenda(LocalDate.of(2026, 5, 19), null).getFirst().hora())
				.isEqualTo(LocalTime.of(8, 0));
	}

	@Test
	void anularHorarioBloqueaElSlotComoNoCitar() {
		Profesional profesional = profesionalRepository.save(profesional("BARRIOS DALMA"));
		LocalDate dia = LocalDate.of(2026, 5, 19);
		LocalTime hora = LocalTime.of(17, 30);

		Turno turno = turnoService.anularHorario(dia, hora, profesional.getId(), "Administracion");

		assertThat(turno.getPresentismo()).isEqualTo(Presentismo.CANCELADO);
		assertThat(turno.getPaciente()).isNull();
		assertThat(turno.getObservacion()).isEqualTo("NO CITAR ESTE TURNO");
		assertThat(turno.getDadoModificadoPor()).isEqualTo("Administracion");
	}

	private Profesional profesional(String nombre) {
		Profesional profesional = new Profesional();
		profesional.setApellidoNombre(nombre);
		profesional.setHabilitado(true);
		return profesional;
	}

	private Turno turno(Profesional profesional, LocalDate dia, LocalTime hora, boolean sobreturno) {
		Turno turno = new Turno();
		turno.setProfesional(profesional);
		turno.setDia(dia);
		turno.setHora(hora);
		turno.setSobreturno(sobreturno);
		return turno;
	}
}
