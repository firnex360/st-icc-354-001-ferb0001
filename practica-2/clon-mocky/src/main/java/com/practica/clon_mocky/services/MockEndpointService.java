package com.practica.clon_mocky.services;

import com.practica.clon_mocky.entities.MockEndpoint;
import com.practica.clon_mocky.entities.Proyecto;
import com.practica.clon_mocky.enums.HttpMetodo;
import com.practica.clon_mocky.repositories.MockEndpointRepository;
import com.practica.clon_mocky.repositories.ProyectoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de gestión de MockEndpoints.
 * <p>
 * Contiene la lógica de negocio para crear, consultar, actualizar y eliminar mocks,
 * así como la lógica de expiración y la resolución de mocks para consumo externo.
 * </p>
 */
@Service
public class MockEndpointService {

    private static final Logger logger = LoggerFactory.getLogger(MockEndpointService.class);

    private final MockEndpointRepository mockRepository;
    private final ProyectoRepository proyectoRepository;
    private final JwtService jwtService;

    public MockEndpointService(MockEndpointRepository mockRepository,
                               ProyectoRepository proyectoRepository,
                               JwtService jwtService) {
        this.mockRepository = mockRepository;
        this.proyectoRepository = proyectoRepository;
        this.jwtService = jwtService;
    }

    // ========================
    // CRUD Operations
    // ========================

    /**
     * Lista todos los mocks del sistema (solo para administradores).
     *
     * @return lista de todos los mocks
     */
    @Transactional(readOnly = true)
    public List<MockEndpoint> listarTodos() {
        return mockRepository.findAll();
    }

    /**
     * Lista todos los mocks de un proyecto específico.
     *
     * @param proyectoId ID del proyecto
     * @return lista de mocks del proyecto ordenados por fecha
     */
    @Transactional(readOnly = true)
    public List<MockEndpoint> listarPorProyecto(Long proyectoId) {
        return mockRepository.findByProyectoIdOrderByFechaCreacionDesc(proyectoId);
    }

    /**
     * Lista todos los mocks de un usuario (a través de sus proyectos).
     *
     * @param usuarioId ID del usuario
     * @return lista de mocks del usuario
     */
    @Transactional(readOnly = true)
    public List<MockEndpoint> listarPorUsuario(Long usuarioId) {
        return mockRepository.findByUsuarioId(usuarioId);
    }

    /**
     * Busca un mock por su ID interno.
     *
     * @param id ID del mock
     * @return Optional con el mock
     */
    @Transactional(readOnly = true)
    public Optional<MockEndpoint> buscarPorId(Long id) {
        return mockRepository.findById(id);
    }

    /**
     * Busca un mock por su UUID público.
     *
     * @param uuid UUID del mock
     * @return Optional con el mock
     */
    @Transactional(readOnly = true)
    public Optional<MockEndpoint> buscarPorUuid(String uuid) {
        return mockRepository.findByUuid(uuid);
    }

    /**
     * Crea un nuevo mock y lo asigna a un proyecto.
     * <p>
     * Si el mock requiere JWT, se genera automáticamente un token cuya
     * expiración coincide con la fecha de expiración del mock.
     * La fecha de expiración se calcula a partir del {@code tipoExpiracion} seleccionado.
     * </p>
     *
     * @param mock       datos del mock
     * @param proyectoId ID del proyecto al que pertenece
     * @return el mock creado con UUID y fechas generadas
     * @throws IllegalArgumentException si el proyecto no existe
     */
    @Transactional
    public MockEndpoint crearMock(MockEndpoint mock, Long proyectoId) {
        Proyecto proyecto = proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new IllegalArgumentException("Proyecto no encontrado con ID: " + proyectoId));

        mock.setProyecto(proyecto);

        // Calcular fecha de expiración basada en el tipo seleccionado
        LocalDateTime ahora = LocalDateTime.now();
        mock.setFechaCreacion(ahora);
        mock.setFechaExpiracion(mock.getTipoExpiracion().calcularExpiracion(ahora));

        // Guardar primero para generar el UUID (@PrePersist)
        MockEndpoint guardado = mockRepository.save(mock);

        // Si requiere JWT, generar token con la misma expiración que el mock
        if (guardado.isRequiereJwt()) {
            String token = jwtService.generarTokenParaMock(guardado.getUuid(), guardado.getFechaExpiracion());
            guardado.setTokenJwt(token);
            guardado = mockRepository.save(guardado);
        }

        logger.info("Mock creado: '{}' [{}] UUID: {} | Expira: {} | JWT: {}",
                guardado.getNombre(), guardado.getMetodo(), guardado.getUuid(),
                guardado.getFechaExpiracion(), guardado.isRequiereJwt());

        return guardado;
    }

    /**
     * Actualiza un mock existente.
     * Recalcula la fecha de expiración si cambió el tipo.
     * Regenera el token JWT si cambió el estado de requiereJwt.
     *
     * @param id   ID del mock a actualizar
     * @param mock datos actualizados
     * @return el mock actualizado
     * @throws IllegalArgumentException si el mock no existe
     */
    @Transactional
    public MockEndpoint actualizarMock(Long id, MockEndpoint mock) {
        MockEndpoint existente = mockRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mock no encontrado con ID: " + id));

        existente.setNombre(mock.getNombre());
        existente.setDescripcion(mock.getDescripcion());
        existente.setRuta(mock.getRuta());
        existente.setMetodo(mock.getMetodo());
        existente.setCodigoRespuesta(mock.getCodigoRespuesta());
        existente.setContentType(mock.getContentType());
        existente.setBody(mock.getBody());
        existente.setHeaders(mock.getHeaders());
        existente.setDelay(mock.getDelay());
        existente.setActivo(mock.isActivo());

        // Si cambió el tipo de expiración, recalcular la fecha
        if (mock.getTipoExpiracion() != existente.getTipoExpiracion()) {
            existente.setTipoExpiracion(mock.getTipoExpiracion());
            existente.setFechaExpiracion(mock.getTipoExpiracion().calcularExpiracion(existente.getFechaCreacion()));
        }

        // Gestionar JWT
        existente.setRequiereJwt(mock.isRequiereJwt());
        if (mock.isRequiereJwt() && (existente.getTokenJwt() == null || existente.getTokenJwt().isBlank())) {
            String token = jwtService.generarTokenParaMock(existente.getUuid(), existente.getFechaExpiracion());
            existente.setTokenJwt(token);
        } else if (!mock.isRequiereJwt()) {
            existente.setTokenJwt(null);
        }

        MockEndpoint actualizado = mockRepository.save(existente);
        logger.info("Mock actualizado: '{}' UUID: {}", actualizado.getNombre(), actualizado.getUuid());
        return actualizado;
    }

    /**
     * Elimina un mock por su ID.
     *
     * @param id ID del mock a eliminar
     * @throws IllegalArgumentException si el mock no existe
     */
    @Transactional
    public void eliminarMock(Long id) {
        MockEndpoint mock = mockRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mock no encontrado con ID: " + id));

        mockRepository.delete(mock);
        logger.info("Mock eliminado: '{}' UUID: {}", mock.getNombre(), mock.getUuid());
    }

    // ========================
    // Lógica de Consumo
    // ========================

    /**
     * Resuelve un mock para consumo externo.
     * <p>
     * Valida que el mock exista, esté activo, no haya expirado y que el método HTTP coincida.
     * Si el mock requiere JWT, se valida el token proporcionado.
     * </p>
     *
     * @param uuid       UUID del mock
     * @param metodo     método HTTP de la petición entrante
     * @param tokenJwt   token JWT proporcionado (puede ser null si el mock no requiere JWT)
     * @return el MockEndpoint resuelto listo para generar la respuesta
     * @throws MockNotFoundException    si el mock no existe o no coincide el método
     * @throws MockExpiredException     si el mock ha expirado
     * @throws MockUnauthorizedException si se requiere JWT y no es válido
     */
    @Transactional(readOnly = true)
    public MockEndpoint resolverMock(String uuid, HttpMetodo metodo, String tokenJwt) {
        // Buscar mock activo por UUID y método
        MockEndpoint mock = mockRepository.findByUuidAndMetodoAndActivoTrue(uuid, metodo)
                .orElseThrow(() -> new MockNotFoundException(
                        "Mock no encontrado para UUID: " + uuid + " con método: " + metodo));

        // Verificar expiración
        if (mock.estaExpirado()) {
            mock.setActivo(false);
            mockRepository.save(mock);
            throw new MockExpiredException("El mock '" + mock.getNombre() + "' ha expirado");
        }

        // Verificar JWT si es requerido
        if (mock.isRequiereJwt()) {
            if (tokenJwt == null || tokenJwt.isBlank()) {
                throw new MockUnauthorizedException("Este mock requiere autenticación JWT");
            }
            if (!jwtService.validarTokenMock(tokenJwt, uuid)) {
                throw new MockUnauthorizedException("Token JWT inválido o expirado para este mock");
            }
        }

        return mock;
    }

    // ========================
    // Lógica de Expiración
    // ========================

    /**
     * Desactiva todos los mocks cuya fecha de expiración ya pasó.
     * Se puede invocar periódicamente (scheduler) o bajo demanda.
     *
     * @return cantidad de mocks desactivados
     */
    @Transactional
    public int desactivarMocksExpirados() {
        List<MockEndpoint> expirados = mockRepository.findByFechaExpiracionBeforeAndActivoTrue(LocalDateTime.now());
        expirados.forEach(mock -> {
            mock.setActivo(false);
            logger.info("Mock expirado desactivado: '{}' UUID: {}", mock.getNombre(), mock.getUuid());
        });
        mockRepository.saveAll(expirados);
        return expirados.size();
    }

    // ========================
    // Excepciones personalizadas
    // ========================

    /** Excepción: Mock no encontrado */
    public static class MockNotFoundException extends RuntimeException {
        public MockNotFoundException(String message) {
            super(message);
        }
    }

    /** Excepción: Mock expirado */
    public static class MockExpiredException extends RuntimeException {
        public MockExpiredException(String message) {
            super(message);
        }
    }

    /** Excepción: Acceso no autorizado al mock */
    public static class MockUnauthorizedException extends RuntimeException {
        public MockUnauthorizedException(String message) {
            super(message);
        }
    }
}
