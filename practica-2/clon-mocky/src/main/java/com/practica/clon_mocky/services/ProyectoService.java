package com.practica.clon_mocky.services;

import com.practica.clon_mocky.entities.Proyecto;
import com.practica.clon_mocky.entities.Usuario;
import com.practica.clon_mocky.repositories.ProyectoRepository;
import com.practica.clon_mocky.repositories.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de gestión de proyectos.
 * <p>
 * Un proyecto agrupa mocks y pertenece a un usuario.
 * Los usuarios normales solo ven sus propios proyectos; el admin ve todos.
 * </p>
 */
@Service
public class ProyectoService {

    private static final Logger logger = LoggerFactory.getLogger(ProyectoService.class);

    private final ProyectoRepository proyectoRepository;
    private final UsuarioRepository usuarioRepository;

    public ProyectoService(ProyectoRepository proyectoRepository, UsuarioRepository usuarioRepository) {
        this.proyectoRepository = proyectoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Lista todos los proyectos del sistema (solo para administradores).
     *
     * @return lista de todos los proyectos
     */
    @Transactional(readOnly = true)
    public List<Proyecto> listarTodos() {
        return proyectoRepository.findAllWithMocksAndUsuario();
    }

    /**
     * Lista los proyectos de un usuario específico, ordenados por fecha de creación.
     *
     * @param usuarioId ID del usuario propietario
     * @return lista de proyectos del usuario
     */
    @Transactional(readOnly = true)
    public List<Proyecto> listarPorUsuario(Long usuarioId) {
        return proyectoRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);
    }

    /**
     * Busca un proyecto por su ID.
     *
     * @param id ID del proyecto
     * @return Optional con el proyecto
     */
    @Transactional(readOnly = true)
    public Optional<Proyecto> buscarPorId(Long id) {
        return proyectoRepository.findByIdWithMocksAndUsuario(id);
    }

    /**
     * Crea un nuevo proyecto asignado a un usuario.
     *
     * @param proyecto  datos del proyecto
     * @param usuarioId ID del usuario propietario
     * @return el proyecto creado
     * @throws IllegalArgumentException si el usuario no existe
     */
    @Transactional
    public Proyecto crearProyecto(Proyecto proyecto, Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + usuarioId));

        proyecto.setUsuario(usuario);

        Proyecto guardado = proyectoRepository.save(proyecto);
        logger.info("Proyecto creado: '{}' para usuario: {}", guardado.getNombre(), usuario.getUsername());
        return guardado;
    }

    /**
     * Actualiza un proyecto existente.
     *
     * @param id       ID del proyecto a actualizar
     * @param proyecto datos actualizados
     * @return el proyecto actualizado
     * @throws IllegalArgumentException si el proyecto no existe
     */
    @Transactional
    public Proyecto actualizarProyecto(Long id, Proyecto proyecto) {
        Proyecto existente = proyectoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proyecto no encontrado con ID: " + id));

        existente.setNombre(proyecto.getNombre());
        existente.setDescripcion(proyecto.getDescripcion());

        Proyecto actualizado = proyectoRepository.save(existente);
        logger.info("Proyecto actualizado: '{}'", actualizado.getNombre());
        return actualizado;
    }

    /**
     * Elimina un proyecto y todos sus mocks asociados (cascade).
     *
     * @param id ID del proyecto a eliminar
     * @throws IllegalArgumentException si el proyecto no existe
     */
    @Transactional
    public void eliminarProyecto(Long id) {
        Proyecto proyecto = proyectoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proyecto no encontrado con ID: " + id));

        proyectoRepository.delete(proyecto);
        logger.info("Proyecto eliminado: '{}' (con {} mocks)", proyecto.getNombre(),
                proyecto.getMocks() != null ? proyecto.getMocks().size() : 0);
    }

    /**
     * Verifica si un proyecto pertenece a un usuario específico.
     *
     * @param proyectoId ID del proyecto
     * @param usuarioId  ID del usuario
     * @return true si el proyecto pertenece al usuario
     */
    @Transactional(readOnly = true)
    public boolean perteneceAUsuario(Long proyectoId, Long usuarioId) {
        return proyectoRepository.findById(proyectoId)
                .map(p -> p.getUsuario().getId().equals(usuarioId))
                .orElse(false);
    }
}
