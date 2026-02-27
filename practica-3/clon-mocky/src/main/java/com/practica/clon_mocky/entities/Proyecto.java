package com.practica.clon_mocky.entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un Proyecto dentro del sistema.
 * <p>
 * Cada proyecto pertenece a un usuario y agrupa múltiples MockEndpoints.
 * Permite organizar los mocks de forma lógica (por aplicación, equipo, etc.).
 * </p>
 */
@Entity
@Table(name = "proyectos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proyecto implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre del proyecto */
    @Column(nullable = false, length = 100)
    private String nombre;

    /** Descripción opcional del proyecto */
    @Column(length = 500)
    private String descripcion;

    /** Fecha de creación del proyecto, se asigna automáticamente */
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /** Usuario propietario del proyecto */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario usuario;

    /** Lista de mocks que pertenecen a este proyecto */
    @OneToMany(mappedBy = "proyecto", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<MockEndpoint> mocks = new ArrayList<>();

    /**
     * Callback de JPA que se ejecuta antes de persistir la entidad.
     * Asigna automáticamente la fecha de creación.
     */
    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
    }
}
