package com.analistas.agendApp.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.analistas.agendApp.service.IUsuarioService;

@ControllerAdvice
public class UsuarioActualAdvice {
	private final IUsuarioService usuarioService;

	public UsuarioActualAdvice(IUsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}

	@ModelAttribute("usuarioActualNombre")
	public String usuarioActualNombre(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
			return "";
		}
		return usuarioService.nombreCompletoActual();
	}
}
