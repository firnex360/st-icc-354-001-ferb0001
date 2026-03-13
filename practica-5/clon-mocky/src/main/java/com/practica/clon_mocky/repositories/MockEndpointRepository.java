package com.practica.clon_mocky.repositories;

import com.practica.clon_mocky.entities.MockEndpoint;
import com.practica.clon_mocky.enums.HttpMetodo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad MockEndpoint.
 * Incluye consultas para búsqueda por UUID, proyecto, usuario y estado de expiración.
 */
@Repository
public interface MockEndpointRepository extends JpaRepository<MockEndpoint, Long> {

    /**
     * Busca un mock por su UUID público (usado en la URL de consumo).
     *
     * @param uuid identificador público del mock
     * @return Optional con el mock si existe
     */
    Optional<MockEndpoint> findByUuid(String uuid);

    /**
     * Obtiene todos los mocks de un proyecto específico.
     *
     * @param proyectoId ID del proyecto
     * @return lista de mocks del proyecto
     */
    List<MockEndpoint> findByProyectoId(Long proyectoId);

    /**
     * Obtiene todos los mocks de un proyecto, ordenados por fecha de creación descendente.
     *
     * @param proyectoId ID del proyecto
     * @return lista de mocks ordenada
     */
    List<MockEndpoint> findByProyectoIdOrderByFechaCreacionDesc(Long proyectoId);

    /**
     * Obtiene todos los mocks de un usuario específico (a través del proyecto).
     *
     * @param usuarioId ID del usuario propietario
     * @return lista de mocks del usuario
     */
    @Query("SELECT m FROM MockEndpoint m WHERE m.proyecto.usuario.id = :usuarioId ORDER BY m.fechaCreacion DESC")
    List<MockEndpoint> findByUsuarioId(@Param("usuarioId") Long usuarioId);

    /**
     * Obtiene los mocks expirados (fecha de expiración anterior a la fecha proporcionada).
     *
     * @param fecha fecha de referencia (normalmente now())
     * @return lista de mocks expirados
     */
    List<MockEndpoint> findByFechaExpiracionBeforeAndActivoTrue(LocalDateTime fecha);

    /**
     * Busca un mock activo por UUID y método HTTP (para validar al consumir el mock).
     *
     * @param uuid   UUID del mock
     * @param metodo método HTTP de la petición
     * @return Optional con el mock si existe, está activo y coincide el método
     */
    @Query("SELECT m FROM MockEndpoint m WHERE m.uuid = :uuid AND m.metodo = :metodo AND m.activo = true")
    Optional<MockEndpoint> findByUuidAndMetodoAndActivoTrue(@Param("uuid") String uuid, @Param("metodo") HttpMetodo metodo);
}
