package com.pucmm.avanzada.jms.repository;

import com.pucmm.avanzada.jms.model.SensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para la entidad SensorData.
 * Proporciona operaciones CRUD automáticas sobre la tabla sensor_data.
 */
@Repository
public interface SensorDataRepository extends JpaRepository<SensorData, Long> {

    /** Obtener todas las lecturas de un dispositivo específico, ordenadas por ID */
    List<SensorData> findByIdDispositivoOrderByIdAsc(Integer idDispositivo);
}
