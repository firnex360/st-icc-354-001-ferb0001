package com.practica.clon_mocky.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

/**
 * Configuración web para internacionalización (i18n).
 * <p>
 * Permite cambiar el idioma de la aplicación mediante el parámetro de URL {@code ?lang=es}
 * o {@code ?lang=en}. El idioma seleccionado se almacena en la sesión HTTP.
 * </p>
 * <p>
 * Idioma por defecto: Español (es).
 * </p>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Resuelve el Locale del usuario a partir de la sesión HTTP.
     * Si no tiene uno guardado, usa español como idioma por defecto.
     *
     * @return LocaleResolver basado en sesión
     */
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver resolver = new SessionLocaleResolver();
        resolver.setDefaultLocale(Locale.of("es")); // Español por defecto
        return resolver;
    }

    /**
     * Interceptor que detecta el parámetro {@code lang} en la URL
     * y cambia el Locale del usuario en la sesión.
     * <p>
     * Ejemplo: {@code /dashboard?lang=en} cambia a inglés.
     * </p>
     *
     * @return interceptor de cambio de idioma
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    /**
     * Registra el interceptor de cambio de idioma en la cadena de MVC.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
