package com.practica.clon_mocky.repositories;

import com.practica.clon_mocky.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad Usuario.
 * Provee operaciones CRUD y consultas personalizadas sobre usuarios.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su nombre de usuario (login).
     *
     * @param username nombre de usuario
     * @return Optional con el usuario si existe
     */
    Optional<Usuario> findByUsername(String username);

    /**
     * Verifica si ya existe un usuario con ese nombre de usuario.
     *
     * @param username nombre de usuario
     * @return true si existe
     */
    boolean existsByUsername(String username);
}
