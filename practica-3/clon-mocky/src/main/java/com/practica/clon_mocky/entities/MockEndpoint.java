package com.practica.clon_mocky.entities;

import com.practica.clon_mocky.enums.HttpMetodo;
import com.practica.clon_mocky.enums.TipoExpiracion;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Entidad que representa un endpoint mockeado (Mock).
 * <p>
 * Almacena toda la configuración necesaria para simular una respuesta HTTP:
 * ruta, método, headers, código de respuesta, body, content-type, delay y expiración.
 * </p>
 * <p>
 * Cada mock tiene un UUID público único que se usa en la URL para consumirlo
 * (ej: /mock/{uuid}). Opcionalmente puede requerir autenticación JWT.
 * </p>
 */
@Entity
@Table(name = "mock_endpoints")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MockEndpoint implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Identificador público único del mock.
     * Se genera automáticamente con UUID y se usa en la URL de consumo.
     */
    @Column(unique = true, nullable = false, updatable = false, length = 36)
    private String uuid;

    /** Nombre descriptivo del mock */
    @Column(nullable = false, length = 100)
    private String nombre;

    /** Descripción opcional del propósito del mock */
    @Column(length = 500)
    private String descripcion;

    /**
     * Ruta del endpoint simulado (ej: /api/users, /products/{id}).
     * Se usa como referencia visual; el consumo real se hace vía /mock/{uuid}.
     */
    @Column(nullable = false, length = 255)
    private String ruta;

    /** Método HTTP que simula este mock (GET, POST, PUT, etc.) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private HttpMetodo metodo;

    /** Código de respuesta HTTP que retornará el mock (200, 404, 500, etc.) */
    @Column(nullable = false)
    private int codigoRespuesta;

    /** Content-Type de la respuesta (ej: application/json, text/xml) */
    @Column(nullable = false, length = 100)
    private String contentType;

    /** Cuerpo de la respuesta que retornará el mock */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String body;

    /**
     * Headers personalizados que se incluirán en la respuesta del mock.
     * Almacenados como pares clave-valor en una tabla auxiliar.
     */
    @ElementCollection
    @CollectionTable(name = "mock_headers", joinColumns = @JoinColumn(name = "mock_id"))
    @MapKeyColumn(name = "header_key")
    @Column(name = "header_value")
    @Builder.Default
    private Map<String, String> headers = new HashMap<>();

    /** Demora en segundos antes de retornar la respuesta (simula latencia) */
    @Builder.Default
    @Column(nullable = false)
    private int delay = 0;

    /** Tipo de expiración seleccionado (1 hora, 1 día, 1 semana, 1 mes, 1 año) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoExpiracion tipoExpiracion;

    /** Fecha calculada en la que el mock deja de estar disponible */
    @Column(nullable = false)
    private LocalDateTime fechaExpiracion;

    /**
     * Indica si el consumo de este mock requiere un token JWT válido.
     * Si es true, se genera un token JWT que expira en la misma fecha que el mock.
     */
    @Builder.Default
    @Column(nullable = false)
    private boolean requiereJwt = false;

    /**
     * Token JWT pre-generado para acceder a este mock (si requiereJwt=true).
     * El token comparte la misma fecha de expiración que el mock.
     */
    @Column(length = 1024)
    private String tokenJwt;

    /** Fecha de creación del mock, se asigna automáticamente */
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /** Indica si el mock está activo y disponible para consumo */
    @Builder.Default
    @Column(nullable = false)
    private boolean activo = true;

    /** Proyecto al que pertenece este mock */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Proyecto proyecto;

    /**
     * Callback de JPA que se ejecuta antes de persistir la entidad.
     * Genera el UUID, asigna la fecha de creación y calcula la fecha de expiración.
     */
    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID().toString();
        }
        if (this.tipoExpiracion != null && this.fechaExpiracion == null) {
            this.fechaExpiracion = this.tipoExpiracion.calcularExpiracion(this.fechaCreacion);
        }
    }

    /**
     * Verifica si el mock ha expirado comparando con la fecha actual.
     *
     * @return true si la fecha de expiración ya pasó
     */
    public boolean estaExpirado() {
        return LocalDateTime.now().isAfter(this.fechaExpiracion);
    }
}
