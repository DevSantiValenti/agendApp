package com.analistas.agendApp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.analistas.agendApp.model.Usuario;

public interface IUsuarioRepository extends JpaRepository<Usuario, Long> {
	Optional<Usuario> findByNombreUsuario(String nombreUsuario);
	boolean existsByNombreUsuario(String nombreUsuario);
	boolean existsByNombreUsuarioAndIdNot(String nombreUsuario, Long id);
	List<Usuario> findAllByOrderByNombreCompletoAsc();
}
