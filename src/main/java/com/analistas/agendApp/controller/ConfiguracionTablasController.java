package com.analistas.agendApp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.analistas.agendApp.model.Especialidad;
import com.analistas.agendApp.model.Feriado;
import com.analistas.agendApp.model.Laboratorio;
import com.analistas.agendApp.model.ObraSocial;
import com.analistas.agendApp.model.PlanObraSocial;
import com.analistas.agendApp.model.Proveedor;
import com.analistas.agendApp.service.IEspecialidadService;
import com.analistas.agendApp.service.IFeriadoService;
import com.analistas.agendApp.service.ILaboratorioService;
import com.analistas.agendApp.service.IObraSocialService;
import com.analistas.agendApp.service.IProveedorService;

@Controller
public class ConfiguracionTablasController {
	private final IObraSocialService obraSocialService;
	private final IEspecialidadService especialidadService;
	private final IFeriadoService feriadoService;
	private final IProveedorService proveedorService;
	private final ILaboratorioService laboratorioService;

	public ConfiguracionTablasController(IObraSocialService obraSocialService, IEspecialidadService especialidadService,
			IFeriadoService feriadoService, IProveedorService proveedorService,
			ILaboratorioService laboratorioService) {
		this.obraSocialService = obraSocialService;
		this.especialidadService = especialidadService;
		this.feriadoService = feriadoService;
		this.proveedorService = proveedorService;
		this.laboratorioService = laboratorioService;
	}

	@GetMapping("/configuracion")
	public String configuracion() {
		return "redirect:/configuracion/tablas/profesionales";
	}

	@GetMapping("/configuracion/tablas/obras-sociales")
	public String obrasSociales(@RequestParam(required = false) String q, Model model) {
		model.addAttribute("obrasSociales", obraSocialService.buscar(q));
		model.addAttribute("obraSocial", new ObraSocial());
		model.addAttribute("plan", new PlanObraSocial());
		model.addAttribute("q", q);
		return "configuracion/tablas/obras-sociales";
	}

	@PostMapping("/configuracion/tablas/obras-sociales")
	public String guardarObraSocial(@ModelAttribute ObraSocial obraSocial) {
		obraSocialService.guardar(obraSocial);
		return "redirect:/configuracion/tablas/obras-sociales";
	}

	@PostMapping("/configuracion/tablas/obras-sociales/{id}/planes")
	public String agregarPlan(@PathVariable Long id, @ModelAttribute PlanObraSocial plan) {
		obraSocialService.agregarPlan(id, plan);
		return "redirect:/configuracion/tablas/obras-sociales";
	}

	@PostMapping("/configuracion/tablas/obras-sociales/{id}/eliminar")
	public String eliminarObraSocial(@PathVariable Long id) {
		obraSocialService.eliminar(id);
		return "redirect:/configuracion/tablas/obras-sociales";
	}

	@GetMapping("/configuracion/tablas/especialidades")
	public String especialidades(@RequestParam(required = false) String q, Model model) {
		model.addAttribute("especialidades", especialidadService.buscar(q));
		model.addAttribute("especialidad", new Especialidad());
		model.addAttribute("q", q);
		return "configuracion/tablas/especialidades";
	}

	@PostMapping("/configuracion/tablas/especialidades")
	public String guardarEspecialidad(@ModelAttribute Especialidad especialidad) {
		especialidadService.guardar(especialidad);
		return "redirect:/configuracion/tablas/especialidades";
	}

	@PostMapping("/configuracion/tablas/especialidades/{id}/eliminar")
	public String eliminarEspecialidad(@PathVariable Long id) {
		especialidadService.eliminar(id);
		return "redirect:/configuracion/tablas/especialidades";
	}

	@GetMapping("/configuracion/tablas/feriados")
	public String feriados(Model model) {
		model.addAttribute("feriados", feriadoService.listar());
		model.addAttribute("feriado", new Feriado());
		return "configuracion/tablas/feriados";
	}

	@PostMapping("/configuracion/tablas/feriados")
	public String guardarFeriado(@ModelAttribute Feriado feriado) {
		feriadoService.guardar(feriado);
		return "redirect:/configuracion/tablas/feriados";
	}

	@PostMapping("/configuracion/tablas/feriados/{id}/eliminar")
	public String eliminarFeriado(@PathVariable Long id) {
		feriadoService.eliminar(id);
		return "redirect:/configuracion/tablas/feriados";
	}

	@GetMapping("/configuracion/tablas/proveedores")
	public String proveedores(@RequestParam(required = false) String q, Model model) {
		model.addAttribute("proveedores", proveedorService.buscar(q));
		model.addAttribute("proveedor", new Proveedor());
		model.addAttribute("q", q);
		return "configuracion/tablas/proveedores";
	}

	@PostMapping("/configuracion/tablas/proveedores")
	public String guardarProveedor(@ModelAttribute Proveedor proveedor) {
		proveedorService.guardar(proveedor);
		return "redirect:/configuracion/tablas/proveedores";
	}

	@PostMapping("/configuracion/tablas/proveedores/{id}/eliminar")
	public String eliminarProveedor(@PathVariable Long id) {
		proveedorService.eliminar(id);
		return "redirect:/configuracion/tablas/proveedores";
	}

	@GetMapping("/configuracion/tablas/laboratorios")
	public String laboratorios(@RequestParam(required = false) String q, Model model) {
		model.addAttribute("laboratorios", laboratorioService.buscar(q));
		model.addAttribute("laboratorio", new Laboratorio());
		model.addAttribute("q", q);
		return "configuracion/tablas/laboratorios";
	}

	@PostMapping("/configuracion/tablas/laboratorios")
	public String guardarLaboratorio(@ModelAttribute Laboratorio laboratorio) {
		laboratorioService.guardar(laboratorio);
		return "redirect:/configuracion/tablas/laboratorios";
	}

	@PostMapping("/configuracion/tablas/laboratorios/{id}/eliminar")
	public String eliminarLaboratorio(@PathVariable Long id) {
		laboratorioService.eliminar(id);
		return "redirect:/configuracion/tablas/laboratorios";
	}
}
