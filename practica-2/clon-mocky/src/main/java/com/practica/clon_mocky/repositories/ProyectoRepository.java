package com.practica.clon_mocky.repositories;

import com.practica.clon_mocky.entities.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Proyecto.
 * Las consultas usan JOIN FETCH para cargar usuario y mocks eagerly
 * y evitar LazyInitializationException en las vistas Thymeleaf.
 */
@Repository
public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {

    /**
     * Obtiene los proyectos de un usuario.
     */
    List<Proyecto> findByUsuarioId(Long usuarioId);

    /**
     * Obtiene los proyectos de un usuario con mocks y usuario cargados.
     */
    @Query("SELECT DISTINCT p FROM Proyecto p LEFT JOIN FETCH p.mocks LEFT JOIN FETCH p.usuario WHERE p.usuario.id = :usuarioId ORDER BY p.fechaCreacion DESC")
    List<Proyecto> findByUsuarioIdOrderByFechaCreacionDesc(@Param("usuarioId") Long usuarioId);

    /**
     * Obtiene todos los proyectos con usuario y mocks cargados.
     */
    @Query("SELECT DISTINCT p FROM Proyecto p LEFT JOIN FETCH p.mocks LEFT JOIN FETCH p.usuario ORDER BY p.fechaCreacion DESC")
    List<Proyecto> findAllWithMocksAndUsuario();

    /**
     * Busca un proyecto por ID con mocks y usuario cargados.
     */
    @Query("SELECT p FROM Proyecto p LEFT JOIN FETCH p.mocks LEFT JOIN FETCH p.usuario WHERE p.id = :id")
    Optional<Proyecto> findByIdWithMocksAndUsuario(@Param("id") Long id);
}
