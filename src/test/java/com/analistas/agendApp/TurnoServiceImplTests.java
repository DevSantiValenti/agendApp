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
import com.analistas.agendApp.model.ProfesionalHorario;
import com.analistas.agendApp.model.Paciente;
import com.analistas.agendApp.model.Presentismo;
import com.analistas.agendApp.model.Turno;
import com.analistas.agendApp.repository.IPacienteRepository;
import com.analistas.agendApp.repository.IProfesionalRepository;
import com.analistas.agendApp.service.ITurnoService;

@SpringBootTest
@Transactional
class TurnoServiceImplTests {
	@Autowired
	private ITurnoService turnoService;

	@Autowired
	private IProfesionalRepository profesionalRepository;

	@Autowired
	private IPacienteRepository pacienteRepository;

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
	void rechazaTurnoCuandoProfesionalNoTieneHorarioEseDia() {
		Profesional profesional = profesionalRepository.save(profesional("SOLER PEDRO"));
		LocalDate domingo = LocalDate.of(2026, 5, 24);

		assertThatThrownBy(() -> turnoService.guardar(turno(profesional, domingo, LocalTime.of(9, 0), false)))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("no tiene horarios configurados para domingo");
	}

	@Test
	void agendaComienzaALasOcho() {
		assertThat(turnoService.construirAgenda(LocalDate.of(2026, 5, 19), null).getFirst().hora())
				.isEqualTo(LocalTime.of(8, 0));
	}

	@Test
	void agendaRespetaHorarioCortadoDelProfesional() {
		Profesional profesional = profesionalRepository.save(profesional("SANCHEZ MAURO",
				horario("Miercoles", LocalTime.of(8, 0), LocalTime.of(12, 30)),
				horario("Miercoles", LocalTime.of(16, 30), LocalTime.of(21, 0))));

		var horas = turnoService.construirAgenda(LocalDate.of(2026, 5, 20), profesional.getId()).stream()
				.map(slot -> slot.hora())
				.toList();

		assertThat(horas).contains(LocalTime.of(8, 0), LocalTime.of(12, 0), LocalTime.of(16, 30), LocalTime.of(20, 30));
		assertThat(horas).doesNotContain(LocalTime.of(12, 30), LocalTime.of(13, 0), LocalTime.of(21, 0));
	}

	@Test
	void agendaSinHorarioDelProfesionalDevuelveListaVacia() {
		Profesional profesional = profesionalRepository.save(profesional("SOLER PEDRO"));

		assertThat(turnoService.construirAgenda(LocalDate.of(2026, 5, 24), profesional.getId())).isEmpty();
	}

	@Test
	void rechazaTurnoEnElCorteDelHorario() {
		Profesional profesional = profesionalRepository.save(profesional("SANCHEZ MAURO",
				horario("Miercoles", LocalTime.of(8, 0), LocalTime.of(12, 30))));

		assertThatThrownBy(() -> turnoService.guardar(turno(profesional, LocalDate.of(2026, 5, 20),
				LocalTime.of(12, 30), false)))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("no atiende en ese horario");
	}

	@Test
	void noPersistePacienteNuevoCuandoElTurnoEsInvalido() {
		Profesional profesional = profesionalRepository.save(profesional("SOLER PEDRO"));
		long pacientesAntes = pacienteRepository.count();
		Turno turno = turno(profesional, LocalDate.of(2026, 5, 24), LocalTime.of(9, 0), false);
		Paciente paciente = new Paciente();
		paciente.setNombreCompleto("Paciente Nuevo");
		turno.setPaciente(paciente);

		assertThatThrownBy(() -> turnoService.guardar(turno))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("no tiene horarios configurados para domingo");
		assertThat(pacienteRepository.count()).isEqualTo(pacientesAntes);
	}

	@Test
	void persistePacienteNuevoCuandoElTurnoEsValido() {
		Profesional profesional = profesionalRepository.save(profesional("BARRIOS DALMA"));
		Turno turno = turno(profesional, LocalDate.of(2026, 5, 19), LocalTime.of(10, 0), false);
		Paciente paciente = new Paciente();
		paciente.setNombreCompleto("Paciente Nuevo");
		paciente.setEmail("paciente@example.com");
		turno.setPaciente(paciente);

		Turno guardado = turnoService.guardar(turno);

		assertThat(guardado.getId()).isNotNull();
		assertThat(guardado.getPaciente().getId()).isNotNull();
		assertThat(guardado.getPaciente().getEmail()).isEqualTo("paciente@example.com");
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
		return profesional(nombre, horario("Martes", LocalTime.of(8, 0), LocalTime.of(20, 0)));
	}

	private Profesional profesional(String nombre, ProfesionalHorario... horarios) {
		Profesional profesional = new Profesional();
		profesional.setApellidoNombre(nombre);
		profesional.setHabilitado(true);
		for (ProfesionalHorario horario : horarios) {
			horario.setProfesional(profesional);
			profesional.getHorarios().add(horario);
		}
		return profesional;
	}

	private ProfesionalHorario horario(String dia, LocalTime desde, LocalTime hasta) {
		ProfesionalHorario horario = new ProfesionalHorario();
		horario.setDia(dia);
		horario.setHoraDesde(desde);
		horario.setHoraHasta(hasta);
		return horario;
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
