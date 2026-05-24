package com.analistas.agendApp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.analistas.agendApp.model.Usuario;
import com.analistas.agendApp.service.IUsuarioService;

@Controller
public class UsuarioController {
	private final IUsuarioService usuarioService;

	public UsuarioController(IUsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}

	@GetMapping("/configuracion/usuarios")
	public String usuarios(Model model) {
		model.addAttribute("usuarios", usuarioService.listar());
		if (!model.containsAttribute("usuario")) {
			model.addAttribute("usuario", new Usuario());
		}
		if (!model.containsAttribute("editando")) {
			model.addAttribute("editando", false);
		}
		return "configuracion/usuarios";
	}

	@GetMapping("/configuracion/usuarios/{id}/editar")
	public String editar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
		try {
			Usuario usuario = usuarioService.buscarPorId(id);
			usuario.setContrasena("");
			model.addAttribute("usuarios", usuarioService.listar());
			model.addAttribute("usuario", usuario);
			model.addAttribute("editando", true);
			return "configuracion/usuarios";
		} catch (RuntimeException ex) {
			redirectAttributes.addFlashAttribute("error", ex.getMessage());
			return "redirect:/configuracion/usuarios";
		}
	}

	@PostMapping("/configuracion/usuarios")
	public String guardar(@ModelAttribute Usuario usuario, RedirectAttributes redirectAttributes) {
		try {
			usuarioService.guardar(usuario);
			redirectAttributes.addFlashAttribute("ok", "Usuario creado.");
		} catch (RuntimeException ex) {
			redirectAttributes.addFlashAttribute("error", ex.getMessage());
		}
		return "redirect:/configuracion/usuarios";
	}

	@PostMapping("/configuracion/usuarios/{id}")
	public String actualizar(@PathVariable Long id, @ModelAttribute Usuario usuario, RedirectAttributes redirectAttributes) {
		try {
			usuarioService.actualizar(id, usuario);
			redirectAttributes.addFlashAttribute("ok", "Usuario actualizado.");
		} catch (RuntimeException ex) {
			redirectAttributes.addFlashAttribute("error", ex.getMessage());
			redirectAttributes.addFlashAttribute("usuario", usuario);
			redirectAttributes.addFlashAttribute("editando", true);
			return "redirect:/configuracion/usuarios/" + id + "/editar";
		}
		return "redirect:/configuracion/usuarios";
	}

	@PostMapping("/configuracion/usuarios/{id}/eliminar")
	public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		try {
			usuarioService.eliminar(id);
			redirectAttributes.addFlashAttribute("ok", "Usuario eliminado.");
		} catch (RuntimeException ex) {
			redirectAttributes.addFlashAttribute("error", ex.getMessage());
		}
		return "redirect:/configuracion/usuarios";
	}
}
