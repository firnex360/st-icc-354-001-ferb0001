package com.pucmm.avanzada.jms.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

/**
 * Entidad JPA que representa una lectura de sensor IoT.
 * Se persiste en la base de datos H2 y se transmite vía WebSocket al frontend.
 */
@Entity
@Table(name = "sensor_data")
public class SensorData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Fecha de generación del dato (formato: DD/MM/YYYY HH:mm:ss) */
    @Column(name = "fecha_generacion")
    private String fechaGeneracion;

    /** Identificador del dispositivo IoT */
    @JsonProperty("IdDispositivo")
    @Column(name = "id_dispositivo")
    private Integer idDispositivo;

    /** Temperatura medida (°C) */
    private Double temperatura;

    /** Humedad relativa medida (%) */
    private Double humedad;

    public SensorData() {
    }

    public SensorData(String fechaGeneracion, Integer idDispositivo, Double temperatura, Double humedad) {
        this.fechaGeneracion = fechaGeneracion;
        this.idDispositivo = idDispositivo;
        this.temperatura = temperatura;
        this.humedad = humedad;
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(String fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    @JsonProperty("IdDispositivo")
    public Integer getIdDispositivo() {
        return idDispositivo;
    }

    @JsonProperty("IdDispositivo")
    public void setIdDispositivo(Integer idDispositivo) {
        this.idDispositivo = idDispositivo;
    }

    public Double getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(Double temperatura) {
        this.temperatura = temperatura;
    }

    public Double getHumedad() {
        return humedad;
    }

    public void setHumedad(Double humedad) {
        this.humedad = humedad;
    }

    @Override
    public String toString() {
        return "SensorData{" +
                "id=" + id +
                ", fechaGeneracion='" + fechaGeneracion + '\'' +
                ", idDispositivo=" + idDispositivo +
                ", temperatura=" + temperatura +
                ", humedad=" + humedad +
                '}';
    }
}
