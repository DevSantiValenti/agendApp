package com.analistas.agendApp;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.analistas.agendApp.model.Paciente;
import com.analistas.agendApp.repository.IPacienteRepository;

@SpringBootTest
@AutoConfigureMockMvc
class MvcSmokeTests {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private IPacienteRepository pacienteRepository;

	@Test
	@WithMockUser(username = "admin")
	void cargaPantallasPrincipales() throws Exception {
		mockMvc.perform(get("/turnos")).andExpect(status().isOk());
		mockMvc.perform(get("/turnos/agenda").param("fecha", "2026-05-19"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("martes, 19 de mayo de 2026")))
				.andExpect(content().string(containsString("\"slots\"")));
		mockMvc.perform(get("/pacientes")).andExpect(status().isOk());
		mockMvc.perform(get("/profesionales/nuevo")).andExpect(status().isOk());
		mockMvc.perform(get("/configuracion/tablas/especialidades")).andExpect(status().isOk());
		mockMvc.perform(get("/configuracion/tablas/proveedores")).andExpect(status().isOk());
		mockMvc.perform(get("/configuracion/usuarios")).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(username = "admin")
	void buscaPacientePorAjaxYCargaDetalle() throws Exception {
		Paciente paciente = new Paciente();
		paciente.setNombreCompleto("Santa Celia");
		paciente.setDocumento("4527704");
		paciente.setHistoriaClinica("405");
		Paciente guardado = pacienteRepository.save(paciente);

		mockMvc.perform(get("/pacientes/buscar").param("q", "Santa"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Santa Celia")));

		mockMvc.perform(get("/pacientes/{id}/detalle", guardado.getId()))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("4527704")));
	}

	@Test
	void muestraLoginAntesDeTurnos() throws Exception {
		mockMvc.perform(get("/turnos")).andExpect(status().is3xxRedirection());
		mockMvc.perform(get("/login"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Agenda Uno")));
	}
}
