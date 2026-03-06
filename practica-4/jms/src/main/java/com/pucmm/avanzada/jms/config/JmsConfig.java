package com.pucmm.avanzada.jms.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;

/**
 * Configuración JMS para conectarse al broker ActiveMQ.
 * Se habilita el modo pub/sub (topics) en lugar de colas simples.
 */
@Configuration
@EnableJms
public class JmsConfig {

    @Value("${spring.activemq.broker-url}")
    private String brokerUrl;

    @Value("${spring.activemq.user:admin}")
    private String brokerUser;

    @Value("${spring.activemq.password:admin}")
    private String brokerPassword;

    /**
     * Fábrica de conexiones hacia ActiveMQ usando OpenWire (TCP).
     * Incluye credenciales de autenticación.
     */
    @Bean
    public ActiveMQConnectionFactory activeMQConnectionFactory() {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory();
        factory.setBrokerURL(brokerUrl);
        factory.setUserName(brokerUser);
        factory.setPassword(brokerPassword);
        factory.setTrustAllPackages(true);
        return factory;
    }

    /**
     * Container factory configurado para escuchar en TOPICS (pub/sub).
     * setPubSubDomain(true) es clave para que @JmsListener use topics.
     */
    @Bean
    public JmsListenerContainerFactory<?> jmsListenerContainerFactory(
            ActiveMQConnectionFactory activeMQConnectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(activeMQConnectionFactory);
        factory.setPubSubDomain(true); // Habilitar publicación/suscripción (topics)
        factory.setConcurrency("1");
        return factory;
    }

    /**
     * Bean ObjectMapper para serialización/deserialización JSON.
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
