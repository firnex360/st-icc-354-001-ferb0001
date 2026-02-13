package com.practica.clon_mocky.repositories;

import com.practica.clon_mocky.entities.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para la entidad Proyecto.
 * Permite consultar proyectos por usuario propietario.
 */
@Repository
public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {

    /**
     * Obtiene todos los proyectos de un usuario específico.
     *
     * @param usuarioId ID del usuario propietario
     * @return lista de proyectos del usuario
     */
    List<Proyecto> findByUsuarioId(Long usuarioId);

    /**
     * Obtiene todos los proyectos de un usuario, ordenados por fecha de creación descendente.
     *
     * @param usuarioId ID del usuario
     * @return lista de proyectos ordenada por fecha
     */
    List<Proyecto> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);
}
