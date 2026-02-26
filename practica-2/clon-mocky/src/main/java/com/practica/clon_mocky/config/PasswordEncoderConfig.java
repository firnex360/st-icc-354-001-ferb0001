package com.practica.clon_mocky.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuración del codificador de contraseñas.
 * <p>
 * Se separa del SecurityConfig para evitar dependencias circulares
 * con los servicios que necesitan inyectar PasswordEncoder.
 * </p>
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * Bean de BCryptPasswordEncoder para encriptar contraseñas.
     * Se usa en la creación de usuarios y en la validación durante el login.
     *
     * @return instancia de BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
