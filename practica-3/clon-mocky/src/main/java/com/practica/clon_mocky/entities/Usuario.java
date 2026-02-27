package com.practica.clon_mocky.entities;

import com.practica.clon_mocky.enums.Rol;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un usuario del sistema.
 * <p>
 * - El Administrador (ROLE_ADMIN) puede crear usuarios, asignar roles y ver todos los mocks.
 * - Los usuarios normales (ROLE_USER) solo gestionan sus propios proyectos y mocks.
 * </p>
 */
@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre de usuario único para login */
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    /** Contraseña encriptada con BCrypt */
    @Column(nullable = false)
    private String password;

    /** Nombre completo del usuario */
    @Column(nullable = false, length = 100)
    private String nombre;

    /** Indica si el usuario está activo en el sistema */
    @Builder.Default
    @Column(nullable = false)
    private boolean activo = true;

    /**
     * Roles del usuario almacenados como @ElementCollection.
     * Se usa EAGER para tener los roles disponibles durante la autenticación.
     */
    @ElementCollection(targetClass = Rol.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "usuario_roles", joinColumns = @JoinColumn(name = "usuario_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "rol")
    @Builder.Default
    private List<Rol> roles = new ArrayList<>();

    /** Proyectos que pertenecen a este usuario */
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<Proyecto> proyectos = new ArrayList<>();

    /**
     * Verifica si el usuario tiene el rol de Administrador.
     *
     * @return true si el usuario es ADMIN
     */
    public boolean esAdmin() {
        return roles != null && roles.contains(Rol.ROLE_ADMIN);
    }
}
