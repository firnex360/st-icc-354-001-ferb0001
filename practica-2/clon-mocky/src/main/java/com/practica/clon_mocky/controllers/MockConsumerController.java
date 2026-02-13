package com.practica.clon_mocky.controllers;

import com.practica.clon_mocky.entities.MockEndpoint;
import com.practica.clon_mocky.enums.HttpMetodo;
import com.practica.clon_mocky.services.MockEndpointService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

/**
 * Controlador REST que intercepta las llamadas a los mocks.
 * <p>
 * Ruta de consumo: {@code /mock/{uuid}}
 * Acepta cualquier método HTTP y lo valida contra la configuración del mock.
 * Aplica la demora (delay), los headers personalizados, el código de respuesta
 * y el body configurados en el mock.
 * </p>
 */
@RestController
@RequestMapping("/mock")
public class MockConsumerController {

    private static final Logger logger = LoggerFactory.getLogger(MockConsumerController.class);

    private final MockEndpointService mockService;

    public MockConsumerController(MockEndpointService mockService) {
        this.mockService = mockService;
    }

    /**
     * Endpoint principal de consumo de mocks.
     * Acepta todos los métodos HTTP y los valida contra la configuración del mock.
     *
     * @param uuid    UUID público del mock
     * @param request la petición HTTP entrante
     * @return la respuesta simulada configurada en el mock
     */
    @RequestMapping(value = "/{uuid}", method = {
            RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
            RequestMethod.DELETE, RequestMethod.PATCH,
            RequestMethod.HEAD, RequestMethod.OPTIONS
    })
    public ResponseEntity<String> consumirMock(@PathVariable String uuid,
                                                HttpServletRequest request) {
        try {
            // Determinar el método HTTP de la petición
            HttpMetodo metodo = HttpMetodo.valueOf(request.getMethod().toUpperCase());

            // Extraer token JWT del header Authorization (si existe)
            String tokenJwt = null;
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                tokenJwt = authHeader.substring(7).trim();
            }

            logger.info("Consumiendo mock UUID: {} | Método: {} | JWT presente: {}",
                    uuid, metodo, tokenJwt != null);

            // Resolver el mock (valida existencia, expiración, método, JWT)
            MockEndpoint mock = mockService.resolverMock(uuid, metodo, tokenJwt);

            // Aplicar delay (demora) si está configurado
            if (mock.getDelay() > 0) {
                logger.info("Aplicando delay de {} segundo(s) para mock: {}", mock.getDelay(), uuid);
                try {
                    TimeUnit.SECONDS.sleep(mock.getDelay());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            // Construir la respuesta
            HttpHeaders responseHeaders = new HttpHeaders();

            // Agregar Content-Type
            responseHeaders.setContentType(MediaType.parseMediaType(mock.getContentType()));

            // Agregar headers personalizados del mock
            if (mock.getHeaders() != null) {
                mock.getHeaders().forEach(responseHeaders::set);
            }

            // Retornar la respuesta con el código, headers y body configurados
            return new ResponseEntity<>(
                    mock.getBody(),
                    responseHeaders,
                    HttpStatus.valueOf(mock.getCodigoRespuesta())
            );

        } catch (MockEndpointService.MockNotFoundException e) {
            logger.warn("Mock no encontrado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\": \"Mock no encontrado\", \"message\": \"" + e.getMessage() + "\"}");

        } catch (MockEndpointService.MockExpiredException e) {
            logger.warn("Mock expirado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.GONE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\": \"Mock expirado\", \"message\": \"" + e.getMessage() + "\"}");

        } catch (MockEndpointService.MockUnauthorizedException e) {
            logger.warn("Acceso no autorizado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\": \"No autorizado\", \"message\": \"" + e.getMessage() + "\"}");

        } catch (Exception e) {
            logger.error("Error inesperado al consumir mock: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\": \"Error interno\", \"message\": \"" + e.getMessage() + "\"}");
        }
    }
}
