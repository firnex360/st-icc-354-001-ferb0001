package com.pucmm.avanzada.jms.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pucmm.avanzada.jms.model.SensorData;
import com.pucmm.avanzada.jms.repository.SensorDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Listener JMS que se suscribe al topic "notificacion_sensores" de ActiveMQ.
 * Al recibir un mensaje:
 *   1. Parsea el JSON a SensorData
 *   2. Persiste en la base de datos H2
 *   3. Retransmite vía WebSocket al frontend para visualización en tiempo real
 */
@Component
public class SensorMessageListener {

    private static final Logger log = LoggerFactory.getLogger(SensorMessageListener.class);

    private final SensorDataRepository repository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public SensorMessageListener(SensorDataRepository repository,
                                  SimpMessagingTemplate messagingTemplate,
                                  ObjectMapper objectMapper) {
        this.repository = repository;
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Escucha el topic JMS "notificacion_sensores" (pub/sub).
     * Recibe el payload como byte[] (STOMP envía bytes) y lo convierte a String.
     */
    @JmsListener(destination = "notificacion_sensores", containerFactory = "jmsListenerContainerFactory")
    public void onMessage(byte[] messageBytes) {
        try {
            String json = new String(messageBytes, java.nio.charset.StandardCharsets.UTF_8);
            log.info("Mensaje recibido del topic: {}", json);

            // Deserializar JSON a entidad
            SensorData sensorData = objectMapper.readValue(json, SensorData.class);

            // Persistir en H2
            SensorData saved = repository.save(sensorData);
            log.info("Dato persistido: {}", saved);

            // Broadcast vía WebSocket al frontend
            messagingTemplate.convertAndSend("/topic/sensores", saved);
            log.info("Dato enviado por WebSocket a /topic/sensores");

        } catch (Exception e) {
            log.error("Error procesando mensaje JMS: {}", e.getMessage(), e);
        }
    }
}
