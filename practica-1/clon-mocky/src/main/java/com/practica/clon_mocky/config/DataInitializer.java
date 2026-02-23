package com.practica.clon_mocky.config;

import com.practica.clon_mocky.entities.MockEndpoint;
import com.practica.clon_mocky.entities.Proyecto;
import com.practica.clon_mocky.entities.Usuario;
import com.practica.clon_mocky.enums.HttpMetodo;
import com.practica.clon_mocky.enums.Rol;
import com.practica.clon_mocky.enums.TipoExpiracion;
import com.practica.clon_mocky.repositories.MockEndpointRepository;
import com.practica.clon_mocky.repositories.ProyectoRepository;
import com.practica.clon_mocky.repositories.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuración de datos iniciales de la aplicación.
 * <p>
 * Se ejecuta al arrancar la aplicación y crea el usuario Administrador
 * por defecto si no existe, junto con un proyecto de ejemplo y mocks de demostración.
 * <ul>
 *   <li><b>Admin - Username:</b> admin / <b>Password:</b> admin</li>
 *   <li><b>User  - Username:</b> user  / <b>Password:</b> user</li>
 * </ul>
 * </p>
 */
@Configuration
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    /**
     * CommandLineRunner que inicializa los datos por defecto.
     * Crea usuarios, un proyecto de ejemplo y mocks de demostración.
     */
    @Bean
    public CommandLineRunner initData(UsuarioRepository usuarioRepository,
                                      ProyectoRepository proyectoRepository,
                                      MockEndpointRepository mockEndpointRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            // Crear usuario admin si no existe
            Usuario admin;
            if (usuarioRepository.findByUsername("admin").isEmpty()) {
                admin = Usuario.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin"))
                        .nombre("Administrador")
                        .activo(true)
                        .roles(Arrays.asList(Rol.ROLE_ADMIN, Rol.ROLE_USER))
                        .build();
                usuarioRepository.save(admin);
                logger.info("===========================================");
                logger.info("  Usuario Administrador creado por defecto");
                logger.info("  Username: admin | Password: admin");
                logger.info("===========================================");
            } else {
                admin = usuarioRepository.findByUsername("admin").get();
                logger.info("Usuario Administrador ya existe, omitiendo creación.");
            }

            // Crear usuario regular si no existe
            if (usuarioRepository.findByUsername("user").isEmpty()) {
                Usuario user = Usuario.builder()
                        .username("user")
                        .password(passwordEncoder.encode("user"))
                        .nombre("Usuario Demo")
                        .activo(true)
                        .roles(Arrays.asList(Rol.ROLE_USER))
                        .build();
                usuarioRepository.save(user);
                logger.info("  Usuario regular creado: user / user");
            }

            // Crear proyecto de ejemplo si no hay proyectos
            if (proyectoRepository.count() == 0) {
                Proyecto proyecto = Proyecto.builder()
                        .nombre("API de Ejemplo")
                        .descripcion("Proyecto de demostración con mocks de una API REST de usuarios y productos.")
                        .usuario(admin)
                        .build();
                proyectoRepository.save(proyecto);

                // Mock 1: GET usuarios (JSON)
                Map<String, String> headersUsuarios = new HashMap<>();
                headersUsuarios.put("X-Api-Version", "1.0");
                headersUsuarios.put("X-RateLimit-Limit", "100");

                MockEndpoint mockUsuarios = MockEndpoint.builder()
                        .nombre("Listar Usuarios")
                        .descripcion("Retorna una lista de usuarios de ejemplo en formato JSON")
                        .ruta("/api/usuarios")
                        .metodo(HttpMetodo.GET)
                        .codigoRespuesta(200)
                        .contentType("application/json")
                        .body("""
                                [
                                  {"id": 1, "nombre": "Juan Pérez", "email": "juan@example.com", "activo": true},
                                  {"id": 2, "nombre": "María García", "email": "maria@example.com", "activo": true},
                                  {"id": 3, "nombre": "Carlos López", "email": "carlos@example.com", "activo": false}
                                ]""")
                        .headers(headersUsuarios)
                        .delay(0)
                        .tipoExpiracion(TipoExpiracion.UN_ANNO)
                        .requiereJwt(false)
                        .proyecto(proyecto)
                        .build();
                mockEndpointRepository.save(mockUsuarios);

                // Mock 2: POST crear usuario (JSON con delay)
                MockEndpoint mockCrear = MockEndpoint.builder()
                        .nombre("Crear Usuario")
                        .descripcion("Simula la creación de un usuario con respuesta 201 y delay de 2 segundos")
                        .ruta("/api/usuarios")
                        .metodo(HttpMetodo.POST)
                        .codigoRespuesta(201)
                        .contentType("application/json")
                        .body("""
                                {"id": 4, "nombre": "Nuevo Usuario", "email": "nuevo@example.com", "activo": true, "message": "Usuario creado exitosamente"}""")
                        .delay(2)
                        .tipoExpiracion(TipoExpiracion.UN_MES)
                        .requiereJwt(false)
                        .proyecto(proyecto)
                        .build();
                mockEndpointRepository.save(mockCrear);

                // Mock 3: GET error 404 (ejemplo de error)
                MockEndpoint mockError = MockEndpoint.builder()
                        .nombre("Usuario No Encontrado")
                        .descripcion("Simula un error 404 cuando el usuario no existe")
                        .ruta("/api/usuarios/999")
                        .metodo(HttpMetodo.GET)
                        .codigoRespuesta(404)
                        .contentType("application/json")
                        .body("""
                                {"error": "Not Found", "message": "El usuario con ID 999 no fue encontrado", "status": 404}""")
                        .delay(0)
                        .tipoExpiracion(TipoExpiracion.UN_ANNO)
                        .requiereJwt(false)
                        .proyecto(proyecto)
                        .build();
                mockEndpointRepository.save(mockError);

                logger.info("  Proyecto de ejemplo creado con {} mocks", 3);
            }
        };
    }
}
