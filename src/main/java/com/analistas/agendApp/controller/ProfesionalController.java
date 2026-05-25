package com.analistas.agendApp.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.analistas.agendApp.model.Especialidad;
import com.analistas.agendApp.model.Profesional;
import com.analistas.agendApp.model.ProfesionalHorario;
import com.analistas.agendApp.repository.IEspecialidadRepository;
import com.analistas.agendApp.service.IProfesionalService;

@Controller
public class ProfesionalController {
	private final IProfesionalService profesionalService;
	private final IEspecialidadRepository especialidadRepository;

	public ProfesionalController(IProfesionalService profesionalService, IEspecialidadRepository especialidadRepository) {
		this.profesionalService = profesionalService;
		this.especialidadRepository = especialidadRepository;
	}

	@GetMapping("/profesionales")
	public String profesionales() {
		return "redirect:/configuracion/tablas/profesionales";
	}

	@GetMapping("/configuracion/tablas/profesionales")
	public String listar(@RequestParam(required = false) String q, Model model) {
		model.addAttribute("profesionales", profesionalService.buscar(q));
		model.addAttribute("q", q);
		return "profesionales/index";
	}

	@GetMapping("/profesionales/nuevo")
	public String nuevo(Model model) {
		cargarFormulario(model, new Profesional());
		return "profesionales/form";
	}

	@GetMapping("/profesionales/{id}")
	public String editar(@PathVariable Long id, Model model) {
		cargarFormulario(model, profesionalService.obtener(id));
		return "profesionales/form";
	}

	@PostMapping("/profesionales")
	public String guardar(@ModelAttribute Profesional profesional,
			@RequestParam(required = false, name = "especialidadIds") List<Long> especialidadIds) {
		if (especialidadIds != null) {
			List<Especialidad> especialidades = especialidadRepository.findAllById(especialidadIds);
			profesional.setEspecialidades(new HashSet<>(especialidades));
		}
		profesional.getHorarios().removeIf(horario -> horario.getDia() == null || horario.getDia().isBlank()
				|| horario.getHoraDesde() == null || horario.getHoraHasta() == null);
		profesional.getHorarios().forEach(horario -> horario.setProfesional(profesional));
		profesionalService.guardar(profesional);
		return "redirect:/configuracion/tablas/profesionales";
	}

	@PostMapping("/profesionales/{id}/eliminar")
	public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		try {
			profesionalService.eliminar(id);
			redirectAttributes.addFlashAttribute("ok", "Profesional eliminado.");
		} catch (RuntimeException ex) {
			redirectAttributes.addFlashAttribute("error", "No se pudo eliminar el profesional.");
		}
		return "redirect:/configuracion/tablas/profesionales";
	}

	private void cargarFormulario(Model model, Profesional profesional) {
		completarHorarios(profesional);
		model.addAttribute("profesional", profesional);
		model.addAttribute("especialidades", especialidadRepository.findAllByOrderByNombreAsc());
	}

	private void completarHorarios(Profesional profesional) {
		String[] dias = {"Lunes", "Martes", "Miercoles", "Jueves", "Viernes", "Sabado", "Domingo"};
		List<ProfesionalHorario> ordenados = new ArrayList<>();
		for (String dia : dias) {
			List<ProfesionalHorario> horariosDia = profesional.getHorarios().stream()
					.filter(horario -> dia.equals(horario.getDia()))
					.peek(this::limpiarFinDeSemanaIncompleto)
					.sorted(Comparator.comparing(ProfesionalHorario::getHoraDesde,
							Comparator.nullsLast(Comparator.naturalOrder())))
					.collect(java.util.stream.Collectors.toCollection(ArrayList::new));
			while (horariosDia.size() < 2) {
				horariosDia.add(nuevoHorario(dia, profesional));
			}
			ordenados.addAll(horariosDia);
		}
		profesional.getHorarios().clear();
		profesional.getHorarios().addAll(ordenados);
	}

	private void limpiarFinDeSemanaIncompleto(ProfesionalHorario horario) {
		if (!esFinDeSemana(horario.getDia())) {
			return;
		}
		if (horario.getHoraDesde() == null || horario.getHoraHasta() == null) {
			horario.setHoraDesde(null);
			horario.setHoraHasta(null);
			horario.setAgendaOnline(false);
		}
	}

	private boolean esFinDeSemana(String dia) {
		return "Sabado".equals(dia) || "Domingo".equals(dia);
	}

	private ProfesionalHorario nuevoHorario(String dia, Profesional profesional) {
		ProfesionalHorario horario = new ProfesionalHorario();
		horario.setDia(dia);
		horario.setProfesional(profesional);
		return horario;
	}
}
