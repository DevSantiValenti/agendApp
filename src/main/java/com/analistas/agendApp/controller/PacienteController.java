package com.analistas.agendApp.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.analistas.agendApp.dto.PacienteDetalle;
import com.analistas.agendApp.dto.PacienteSearchResult;
import com.analistas.agendApp.model.Paciente;
import com.analistas.agendApp.repository.IObraSocialRepository;
import com.analistas.agendApp.repository.IPlanObraSocialRepository;
import com.analistas.agendApp.service.IPacienteService;

@Controller
public class PacienteController {
	private final IPacienteService pacienteService;
	private final IObraSocialRepository obraSocialRepository;
	private final IPlanObraSocialRepository planRepository;

	public PacienteController(IPacienteService pacienteService, IObraSocialRepository obraSocialRepository,
			IPlanObraSocialRepository planRepository) {
		this.pacienteService = pacienteService;
		this.obraSocialRepository = obraSocialRepository;
		this.planRepository = planRepository;
	}

	@GetMapping("/pacientes")
	public String listar(Model model) {
		return "pacientes/index";
	}

	@GetMapping("/pacientes/buscar")
	@ResponseBody
	public List<PacienteSearchResult> buscarAjax(@RequestParam(required = false) String q) {
		if (q == null || q.isBlank()) {
			return List.of();
		}
		return pacienteService.buscar(q).stream()
				.limit(12)
				.map(this::toSearchResult)
				.toList();
	}

	@GetMapping("/pacientes/{id}/detalle")
	@ResponseBody
	public PacienteDetalle detalleAjax(@PathVariable Long id) {
		return toDetalle(pacienteService.obtener(id));
	}

	@GetMapping("/pacientes/nuevo")
	public String nuevo(Model model) {
		cargarFormulario(model, new Paciente());
		return "pacientes/form";
	}

	@GetMapping("/pacientes/{id}")
	public String editar(@PathVariable Long id, Model model) {
		cargarFormulario(model, pacienteService.obtener(id));
		return "pacientes/form";
	}

	@PostMapping("/pacientes")
	public String guardar(@ModelAttribute Paciente paciente, @RequestParam(required = false) Long obraSocialId,
			@RequestParam(required = false) Long planId) {
		paciente.setObraSocial(obraSocialId == null ? null : obraSocialRepository.findById(obraSocialId).orElse(null));
		paciente.setPlan(planId == null ? null : planRepository.findById(planId).orElse(null));
		pacienteService.guardar(paciente);
		return "redirect:/pacientes";
	}

	@PostMapping("/pacientes/{id}/eliminar")
	public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		try {
			pacienteService.eliminar(id);
			redirectAttributes.addFlashAttribute("ok", "Paciente eliminado.");
		} catch (RuntimeException ex) {
			redirectAttributes.addFlashAttribute("error", "No se pudo eliminar el paciente.");
		}
		return "redirect:/pacientes";
	}

	private void cargarFormulario(Model model, Paciente paciente) {
		model.addAttribute("paciente", paciente);
		model.addAttribute("obrasSociales", obraSocialRepository.findByHabilitadoTrueOrderByNombreAsc());
	}

	private PacienteSearchResult toSearchResult(Paciente paciente) {
		return new PacienteSearchResult(
				paciente.getId(),
				paciente.getNombreCompleto(),
				paciente.getDocumento(),
				paciente.getHistoriaClinica(),
				paciente.getObraSocial() == null ? null : paciente.getObraSocial().getNombre(),
				paciente.getPlan() == null ? null : paciente.getPlan().getNombre(),
				paciente.getFechaNacimiento(),
				paciente.getNumeroAfiliado(),
				paciente.getDomicilio());
	}

	private PacienteDetalle toDetalle(Paciente paciente) {
		return new PacienteDetalle(
				paciente.getId(),
				paciente.getNombreCompleto(),
				paciente.getTipoDocumento(),
				paciente.getDocumento(),
				paciente.getHistoriaClinica(),
				paciente.getFechaNacimiento(),
				paciente.getFechaAlta(),
				paciente.getUltimaAtencion(),
				paciente.getSaldoAcreedor(),
				paciente.getFechaRecitacion(),
				paciente.getMotivoRecitacion(),
				paciente.getObraSocial() == null ? null : paciente.getObraSocial().getNombre(),
				paciente.getPlan() == null ? null : paciente.getPlan().getNombre(),
				paciente.getNumeroAfiliado(),
				paciente.getCondicionAfiliatoria(),
				paciente.getTelefonoCelular(),
				paciente.getTelefonoParticular(),
				paciente.getTelefonoOficina(),
				paciente.getEmail(),
				paciente.getDomicilio(),
				paciente.getLocalidad(),
				paciente.getProvincia(),
				paciente.getCuitCuil(),
				paciente.getCondicionFiscal(),
				paciente.getHepatitis(),
				paciente.getEmbarazo(),
				paciente.getDiabetes(),
				paciente.getAlergia(),
				paciente.getCardiaco(),
				paciente.getRenal(),
				paciente.getHiv(),
				paciente.getObservaciones(),
				paciente.getAlerta());
	}
}
