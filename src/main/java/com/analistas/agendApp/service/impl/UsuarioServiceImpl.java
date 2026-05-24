package com.analistas.agendApp.service.impl;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.analistas.agendApp.model.Usuario;
import com.analistas.agendApp.repository.IUsuarioRepository;
import com.analistas.agendApp.service.IUsuarioService;

@Service
public class UsuarioServiceImpl implements IUsuarioService, UserDetailsService {
	private final IUsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;

	public UsuarioServiceImpl(IUsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Usuario usuario = usuarioRepository.findByNombreUsuario(username)
				.orElseThrow(() -> new UsernameNotFoundException("Usuario inexistente"));
		return User.withUsername(usuario.getNombreUsuario())
				.password(usuario.getContrasena())
				.roles("USER")
				.build();
	}

	@Override
	public List<Usuario> listar() {
		return usuarioRepository.findAllByOrderByNombreCompletoAsc();
	}

	@Override
	public Usuario buscarPorId(Long id) {
		return usuarioRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Usuario inexistente."));
	}

	@Override
	@Transactional
	public Usuario guardar(Usuario usuario) {
		if (usuario.getNombreUsuario() == null || usuario.getNombreUsuario().isBlank()) {
			throw new IllegalArgumentException("El nombre de usuario es obligatorio.");
		}
		if (usuario.getContrasena() == null || usuario.getContrasena().isBlank()) {
			throw new IllegalArgumentException("La contraseña es obligatoria.");
		}
		String nombreUsuario = usuario.getNombreUsuario().trim();
		if (usuarioRepository.existsByNombreUsuario(nombreUsuario)) {
			throw new IllegalArgumentException("Ya existe un usuario con ese nombre.");
		}
		usuario.setNombreUsuario(nombreUsuario);
		usuario.setNombreCompleto(usuario.getNombreCompleto() == null ? "" : usuario.getNombreCompleto().trim());
		usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
		return usuarioRepository.save(usuario);
	}

	@Override
	@Transactional
	public Usuario actualizar(Long id, Usuario datos) {
		Usuario usuario = buscarPorId(id);
		if (datos.getNombreUsuario() == null || datos.getNombreUsuario().isBlank()) {
			throw new IllegalArgumentException("El nombre de usuario es obligatorio.");
		}
		String nombreUsuario = datos.getNombreUsuario().trim();
		if (usuarioRepository.existsByNombreUsuarioAndIdNot(nombreUsuario, id)) {
			throw new IllegalArgumentException("Ya existe un usuario con ese nombre.");
		}
		usuario.setNombreCompleto(datos.getNombreCompleto() == null ? "" : datos.getNombreCompleto().trim());
		usuario.setNombreUsuario(nombreUsuario);
		if (datos.getContrasena() != null && !datos.getContrasena().isBlank()) {
			usuario.setContrasena(passwordEncoder.encode(datos.getContrasena()));
		}
		return usuarioRepository.save(usuario);
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		Usuario usuario = buscarPorId(id);
		if (usuarioRepository.count() <= 1) {
			throw new IllegalArgumentException("No se puede eliminar el último usuario.");
		}
		usuarioRepository.delete(usuario);
	}

	@Override
	public String nombreCompletoActual() {
		String username = SecurityContextHolder.getContext().getAuthentication() == null
				? null
				: SecurityContextHolder.getContext().getAuthentication().getName();
		if (username == null || username.isBlank() || "anonymousUser".equals(username)) {
			return "Sistema";
		}
		return usuarioRepository.findByNombreUsuario(username)
				.map(Usuario::getNombreCompleto)
				.filter(nombre -> !nombre.isBlank())
				.orElse(username);
	}
}
