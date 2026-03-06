package com.pucmm.avanzada.jms.controller;

import com.pucmm.avanzada.jms.model.SensorData;
import com.pucmm.avanzada.jms.repository.SensorDataRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller para consultar datos históricos de sensores.
 * Permite al frontend cargar datos existentes al iniciar.
 */
@RestController
@RequestMapping("/api/sensores")
public class SensorRestController {

    private final SensorDataRepository repository;

    public SensorRestController(SensorDataRepository repository) {
        this.repository = repository;
    }

    /** Obtener todos los datos de sensores almacenados */
    @GetMapping
    public List<SensorData> getAll() {
        return repository.findAll();
    }
}
