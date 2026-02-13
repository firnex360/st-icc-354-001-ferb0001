package com.practica.clon_mocky.config;

import com.practica.clon_mocky.entities.Usuario;
import com.practica.clon_mocky.enums.Rol;
import com.practica.clon_mocky.repositories.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;

/**
 * Configuración de datos iniciales de la aplicación.
 * <p>
 * Se ejecuta al arrancar la aplicación y crea el usuario Administrador
 * por defecto si no existe. El admin tiene credenciales:
 * <ul>
 *   <li><b>Username:</b> admin</li>
 *   <li><b>Password:</b> admin</li>
 * </ul>
 * </p>
 */
@Configuration
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    /**
     * CommandLineRunner que inicializa los datos por defecto.
     * Crea el usuario Administrador con roles ROLE_ADMIN y ROLE_USER
     * si no existe en la base de datos.
     *
     * @param usuarioRepository repositorio de usuarios
     * @param passwordEncoder   codificador de contraseñas
     * @return CommandLineRunner con la lógica de inicialización
     */
    @Bean
    public CommandLineRunner initData(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Solo crear el admin si no existe
            if (usuarioRepository.findByUsername("admin").isEmpty()) {
                Usuario admin = Usuario.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin"))
                        .nombre("Administrador")
                        .activo(true)
                        .roles(Arrays.asList(Rol.ROLE_ADMIN, Rol.ROLE_USER))
                        .build();

                usuarioRepository.save(admin);
                logger.info("===========================================");
                logger.info("  Usuario Administrador creado por defecto");
                logger.info("  Username: admin");
                logger.info("  Password: admin");
                logger.info("===========================================");
            } else {
                logger.info("Usuario Administrador ya existe, omitiendo creación.");
            }
        };
    }
}
