package com.practica.clon_mocky.controllers;

import com.practica.clon_mocky.entities.MockEndpoint;
import com.practica.clon_mocky.entities.Proyecto;
import com.practica.clon_mocky.entities.Usuario;
import com.practica.clon_mocky.enums.HttpMetodo;
import com.practica.clon_mocky.enums.TipoExpiracion;
import com.practica.clon_mocky.services.MockEndpointService;
import com.practica.clon_mocky.services.ProyectoService;
import com.practica.clon_mocky.services.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador de gestión de MockEndpoints.
 * Permite CRUD de mocks dentro de un proyecto.
 */
@Controller
@RequestMapping("/proyectos/{proyectoId}/mocks")
public class MockEndpointController {

    private final MockEndpointService mockService;
    private final ProyectoService proyectoService;
    private final UsuarioService usuarioService;

    public MockEndpointController(MockEndpointService mockService,
                                  ProyectoService proyectoService,
                                  UsuarioService usuarioService) {
        this.mockService = mockService;
        this.proyectoService = proyectoService;
        this.usuarioService = usuarioService;
    }

    private Usuario getUsuarioAutenticado(Authentication auth) {
        return usuarioService.buscarPorUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    /**
     * Verifica que el usuario tenga acceso al proyecto.
     */
    private boolean tieneAcceso(Usuario usuario, Long proyectoId) {
        return usuario.esAdmin() || proyectoService.perteneceAUsuario(proyectoId, usuario.getId());
    }

    /**
     * Lista los mocks de un proyecto.
     */
    @GetMapping
    public String listar(@PathVariable Long proyectoId, Authentication auth,
                         Model model, RedirectAttributes redirect) {
        Usuario usuario = getUsuarioAutenticado(auth);
        if (!tieneAcceso(usuario, proyectoId)) {
            redirect.addFlashAttribute("error", "Sin permisos");
            return "redirect:/proyectos";
        }

        Proyecto proyecto = proyectoService.buscarPorId(proyectoId)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));

        model.addAttribute("proyecto", proyecto);
        model.addAttribute("mocks", mockService.listarPorProyecto(proyectoId));
        model.addAttribute("usuario", usuario);
        return "mocks/lista";
    }

    /**
     * Muestra el formulario para crear un nuevo mock.
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(@PathVariable Long proyectoId, Authentication auth,
                                         Model model, RedirectAttributes redirect) {
        Usuario usuario = getUsuarioAutenticado(auth);
        if (!tieneAcceso(usuario, proyectoId)) {
            redirect.addFlashAttribute("error", "Sin permisos");
            return "redirect:/proyectos";
        }

        Proyecto proyecto = proyectoService.buscarPorId(proyectoId)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));

        model.addAttribute("proyecto", proyecto);
        model.addAttribute("mock", new MockEndpoint());
        model.addAttribute("metodos", HttpMetodo.values());
        model.addAttribute("expiraciones", TipoExpiracion.values());
        model.addAttribute("esNuevo", true);
        return "mocks/form";
    }

    /**
     * Muestra el formulario para editar un mock existente.
     */
    @GetMapping("/editar/{mockId}")
    public String mostrarFormularioEditar(@PathVariable Long proyectoId,
                                          @PathVariable Long mockId,
                                          Authentication auth,
                                          Model model, RedirectAttributes redirect) {
        Usuario usuario = getUsuarioAutenticado(auth);
        if (!tieneAcceso(usuario, proyectoId)) {
            redirect.addFlashAttribute("error", "Sin permisos");
            return "redirect:/proyectos";
        }

        return mockService.buscarPorId(mockId)
                .map(mock -> {
                    Proyecto proyecto = proyectoService.buscarPorId(proyectoId)
                            .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));
                    model.addAttribute("proyecto", proyecto);
                    model.addAttribute("mock", mock);
                    model.addAttribute("metodos", HttpMetodo.values());
                    model.addAttribute("expiraciones", TipoExpiracion.values());
                    model.addAttribute("esNuevo", false);
                    return "mocks/form";
                })
                .orElseGet(() -> {
                    redirect.addFlashAttribute("error", "Mock no encontrado");
                    return "redirect:/proyectos/" + proyectoId + "/mocks";
                });
    }

    /**
     * Muestra el detalle de un mock con su URL y token JWT.
     */
    @GetMapping("/detalle/{mockId}")
    public String detalle(@PathVariable Long proyectoId,
                          @PathVariable Long mockId,
                          Authentication auth,
                          Model model, RedirectAttributes redirect) {
        Usuario usuario = getUsuarioAutenticado(auth);
        if (!tieneAcceso(usuario, proyectoId)) {
            redirect.addFlashAttribute("error", "Sin permisos");
            return "redirect:/proyectos";
        }

        return mockService.buscarPorId(mockId)
                .map(mock -> {
                    Proyecto proyecto = proyectoService.buscarPorId(proyectoId)
                            .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));
                    model.addAttribute("proyecto", proyecto);
                    model.addAttribute("mock", mock);
                    model.addAttribute("usuario", usuario);
                    return "mocks/detalle";
                })
                .orElseGet(() -> {
                    redirect.addFlashAttribute("error", "Mock no encontrado");
                    return "redirect:/proyectos/" + proyectoId + "/mocks";
                });
    }

    /**
     * Crea un nuevo mock dentro del proyecto.
     */
    @PostMapping("/crear")
    public String crear(@PathVariable Long proyectoId,
                        @RequestParam String nombre,
                        @RequestParam(required = false) String descripcion,
                        @RequestParam String ruta,
                        @RequestParam String metodo,
                        @RequestParam int codigoRespuesta,
                        @RequestParam String contentType,
                        @RequestParam(required = false) String body,
                        @RequestParam(required = false) List<String> headerKeys,
                        @RequestParam(required = false) List<String> headerValues,
                        @RequestParam(defaultValue = "0") int delay,
                        @RequestParam String tipoExpiracion,
                        @RequestParam(defaultValue = "false") boolean requiereJwt,
                        Authentication auth,
                        RedirectAttributes redirect) {
        try {
            Usuario usuario = getUsuarioAutenticado(auth);
            if (!tieneAcceso(usuario, proyectoId)) {
                redirect.addFlashAttribute("error", "Sin permisos");
                return "redirect:/proyectos";
            }

            // Construir mapa de headers
            Map<String, String> headers = buildHeaders(headerKeys, headerValues);

            MockEndpoint mock = MockEndpoint.builder()
                    .nombre(nombre)
                    .descripcion(descripcion)
                    .ruta(ruta)
                    .metodo(HttpMetodo.valueOf(metodo))
                    .codigoRespuesta(codigoRespuesta)
                    .contentType(contentType)
                    .body(body)
                    .headers(headers)
                    .delay(delay)
                    .tipoExpiracion(TipoExpiracion.valueOf(tipoExpiracion))
                    .requiereJwt(requiereJwt)
                    .build();

            MockEndpoint creado = mockService.crearMock(mock, proyectoId);
            redirect.addFlashAttribute("success", "msg.save.success");
            return "redirect:/proyectos/" + proyectoId + "/mocks/detalle/" + creado.getId();
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/proyectos/" + proyectoId + "/mocks";
        }
    }

    /**
     * Actualiza un mock existente.
     */
    @PostMapping("/actualizar/{mockId}")
    public String actualizar(@PathVariable Long proyectoId,
                             @PathVariable Long mockId,
                             @RequestParam String nombre,
                             @RequestParam(required = false) String descripcion,
                             @RequestParam String ruta,
                             @RequestParam String metodo,
                             @RequestParam int codigoRespuesta,
                             @RequestParam String contentType,
                             @RequestParam(required = false) String body,
                             @RequestParam(required = false) List<String> headerKeys,
                             @RequestParam(required = false) List<String> headerValues,
                             @RequestParam(defaultValue = "0") int delay,
                             @RequestParam String tipoExpiracion,
                             @RequestParam(defaultValue = "false") boolean requiereJwt,
                             @RequestParam(defaultValue = "true") boolean activo,
                             Authentication auth,
                             RedirectAttributes redirect) {
        try {
            Usuario usuario = getUsuarioAutenticado(auth);
            if (!tieneAcceso(usuario, proyectoId)) {
                redirect.addFlashAttribute("error", "Sin permisos");
                return "redirect:/proyectos";
            }

            Map<String, String> headers = buildHeaders(headerKeys, headerValues);

            MockEndpoint datos = MockEndpoint.builder()
                    .nombre(nombre)
                    .descripcion(descripcion)
                    .ruta(ruta)
                    .metodo(HttpMetodo.valueOf(metodo))
                    .codigoRespuesta(codigoRespuesta)
                    .contentType(contentType)
                    .body(body)
                    .headers(headers)
                    .delay(delay)
                    .tipoExpiracion(TipoExpiracion.valueOf(tipoExpiracion))
                    .requiereJwt(requiereJwt)
                    .activo(activo)
                    .build();

            mockService.actualizarMock(mockId, datos);
            redirect.addFlashAttribute("success", "msg.save.success");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/proyectos/" + proyectoId + "/mocks";
    }

    /**
     * Elimina un mock.
     */
    @PostMapping("/eliminar/{mockId}")
    public String eliminar(@PathVariable Long proyectoId,
                           @PathVariable Long mockId,
                           Authentication auth,
                           RedirectAttributes redirect) {
        try {
            Usuario usuario = getUsuarioAutenticado(auth);
            if (!tieneAcceso(usuario, proyectoId)) {
                redirect.addFlashAttribute("error", "Sin permisos");
                return "redirect:/proyectos";
            }

            mockService.eliminarMock(mockId);
            redirect.addFlashAttribute("success", "msg.delete.success");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/proyectos/" + proyectoId + "/mocks";
    }

    /**
     * Construye el mapa de headers a partir de las listas de claves y valores del formulario.
     */
    private Map<String, String> buildHeaders(List<String> keys, List<String> values) {
        Map<String, String> headers = new HashMap<>();
        if (keys != null && values != null) {
            for (int i = 0; i < keys.size(); i++) {
                String key = keys.get(i).trim();
                String value = i < values.size() ? values.get(i).trim() : "";
                if (!key.isEmpty()) {
                    headers.put(key, value);
                }
            }
        }
        return headers;
    }
}
