package com.analistas.agendApp.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.analistas.agendApp.model.Usuario;
import com.analistas.agendApp.repository.IUsuarioRepository;
import com.analistas.agendApp.service.IUsuarioService;

@Configuration
public class UsuarioInicialConfig {
	@Bean
	CommandLineRunner crearUsuarioAdministrador(IUsuarioRepository usuarioRepository, IUsuarioService usuarioService) {
		return args -> {
			if (usuarioRepository.existsByNombreUsuario("admin")) {
				return;
			}
			Usuario usuario = new Usuario();
			usuario.setNombreCompleto("Administracion");
			usuario.setNombreUsuario("admin");
			usuario.setContrasena("chuflitos1");
			usuarioService.guardar(usuario);
		};
	}
}
