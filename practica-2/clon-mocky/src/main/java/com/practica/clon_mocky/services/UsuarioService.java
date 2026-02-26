package com.practica.clon_mocky.services;

import com.practica.clon_mocky.entities.Usuario;
import com.practica.clon_mocky.enums.Rol;
import com.practica.clon_mocky.repositories.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio de gestión de usuarios.
 * <p>
 * Implementa {@link UserDetailsService} para integrarse con Spring Security.
 * Contiene la lógica de negocio para CRUD de usuarios, validación de roles
 * y carga de credenciales durante la autenticación.
 * </p>
 */
@Service
public class UsuarioService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ========================
    // Spring Security
    // ========================

    /**
     * Carga un usuario por su username para la autenticación de Spring Security.
     * Convierte la entidad Usuario a un UserDetails con sus roles como GrantedAuthority.
     *
     * @param username nombre de usuario
     * @return UserDetails para Spring Security
     * @throws UsernameNotFoundException si el usuario no existe o está inactivo
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Autenticando usuario: {}", username);

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        if (!usuario.isActivo()) {
            throw new UsernameNotFoundException("Usuario inactivo: " + username);
        }

        // Convertir roles del enum a GrantedAuthority
        List<GrantedAuthority> authorities = usuario.getRoles().stream()
                .map(rol -> new SimpleGrantedAuthority(rol.name()))
                .collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(
                usuario.getUsername(),
                usuario.getPassword(),
                usuario.isActivo(),  // enabled
                true,                // accountNonExpired
                true,                // credentialsNonExpired
                true,                // accountNonLocked
                authorities
        );
    }

    // ========================
    // CRUD Operations
    // ========================

    /**
     * Obtiene todos los usuarios del sistema.
     *
     * @return lista de todos los usuarios
     */
    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    /**
     * Busca un usuario por su ID.
     *
     * @param id ID del usuario
     * @return Optional con el usuario
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    /**
     * Busca un usuario por su username.
     *
     * @param username nombre de usuario
     * @return Optional con el usuario
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    /**
     * Crea un nuevo usuario en el sistema.
     * La contraseña se encripta automáticamente con BCrypt.
     *
     * @param usuario datos del usuario a crear
     * @return el usuario creado con ID asignado
     * @throws IllegalArgumentException si el username ya existe
     */
    @Transactional
    public Usuario crearUsuario(Usuario usuario) {
        // Validar que el username no exista
        if (usuarioRepository.existsByUsername(usuario.getUsername())) {
            throw new IllegalArgumentException("El nombre de usuario '" + usuario.getUsername() + "' ya existe");
        }

        // Encriptar contraseña
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        // Si no tiene roles asignados, asignar ROLE_USER por defecto
        if (usuario.getRoles() == null || usuario.getRoles().isEmpty()) {
            usuario.setRoles(new ArrayList<>(List.of(Rol.ROLE_USER)));
        }

        Usuario guardado = usuarioRepository.save(usuario);
        logger.info("Usuario creado: {} con roles: {}", guardado.getUsername(), guardado.getRoles());
        return guardado;
    }

    /**
     * Actualiza un usuario existente.
     * Si la contraseña viene vacía o nula, se mantiene la contraseña actual.
     *
     * @param id      ID del usuario a actualizar
     * @param usuario datos actualizados
     * @return el usuario actualizado
     * @throws IllegalArgumentException si el usuario no existe
     */
    @Transactional
    public Usuario actualizarUsuario(Long id, Usuario usuario) {
        Usuario existente = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));

        existente.setNombre(usuario.getNombre());
        existente.setActivo(usuario.isActivo());
        existente.setRoles(usuario.getRoles());

        // Solo actualizar contraseña si se proporcionó una nueva
        if (usuario.getPassword() != null && !usuario.getPassword().isBlank()) {
            existente.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }

        Usuario actualizado = usuarioRepository.save(existente);
        logger.info("Usuario actualizado: {}", actualizado.getUsername());
        return actualizado;
    }

    /**
     * Elimina un usuario por su ID.
     * No se puede eliminar al usuario administrador principal.
     *
     * @param id ID del usuario a eliminar
     * @throws IllegalArgumentException si se intenta eliminar al admin por defecto
     */
    @Transactional
    public void eliminarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));

        // Protección: no permitir eliminar al admin por defecto
        if ("admin".equals(usuario.getUsername())) {
            throw new IllegalArgumentException("No se puede eliminar al usuario administrador por defecto");
        }

        usuarioRepository.delete(usuario);
        logger.info("Usuario eliminado: {}", usuario.getUsername());
    }

    /**
     * Verifica si ya existe al menos un usuario administrador en el sistema.
     *
     * @return true si existe algún admin
     */
    @Transactional(readOnly = true)
    public boolean existeAdmin() {
        return usuarioRepository.findAll().stream()
                .anyMatch(Usuario::esAdmin);
    }
}
