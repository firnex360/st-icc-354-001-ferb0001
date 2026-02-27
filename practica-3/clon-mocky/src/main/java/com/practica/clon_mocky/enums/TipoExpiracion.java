package com.practica.clon_mocky.enums;

import java.time.LocalDateTime;

/**
 * Enum que define los tipos de expiración disponibles para un MockEndpoint.
 * Cada valor encapsula la lógica para calcular la fecha de expiración
 * a partir de una fecha base (normalmente la fecha de creación del mock).
 */
public enum TipoExpiracion {

    UNA_HORA("1 Hora") {
        @Override
        public LocalDateTime calcularExpiracion(LocalDateTime desde) {
            return desde.plusHours(1);
        }
    },
    UN_DIA("1 Día") {
        @Override
        public LocalDateTime calcularExpiracion(LocalDateTime desde) {
            return desde.plusDays(1);
        }
    },
    UNA_SEMANA("1 Semana") {
        @Override
        public LocalDateTime calcularExpiracion(LocalDateTime desde) {
            return desde.plusWeeks(1);
        }
    },
    UN_MES("1 Mes") {
        @Override
        public LocalDateTime calcularExpiracion(LocalDateTime desde) {
            return desde.plusMonths(1);
        }
    },
    UN_ANNO("1 Año") {
        @Override
        public LocalDateTime calcularExpiracion(LocalDateTime desde) {
            return desde.plusYears(1);
        }
    };

    private final String descripcion;

    TipoExpiracion(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * Calcula la fecha de expiración sumando la duración correspondiente
     * a la fecha base proporcionada.
     *
     * @param desde fecha base desde la cual calcular la expiración
     * @return la fecha de expiración calculada
     */
    public abstract LocalDateTime calcularExpiracion(LocalDateTime desde);

    public String getDescripcion() {
        return descripcion;
    }
}
