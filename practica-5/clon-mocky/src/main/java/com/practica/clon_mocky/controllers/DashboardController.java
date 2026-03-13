package com.practica.clon_mocky.controllers;

import com.practica.clon_mocky.entities.Usuario;
import com.practica.clon_mocky.services.MockEndpointService;
import com.practica.clon_mocky.services.ProyectoService;
import com.practica.clon_mocky.services.UsuarioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador del panel de control principal.
 * Muestra un resumen de proyectos y mocks del usuario autenticado.
 * Si es admin, muestra estadísticas globales.
 */
@Controller
public class DashboardController {

    @Value("${server.port}")
    private String serverPort;

    private final UsuarioService usuarioService;
    private final ProyectoService proyectoService;
    private final MockEndpointService mockEndpointService;

    public DashboardController(UsuarioService usuarioService,
                               ProyectoService proyectoService,
                               MockEndpointService mockEndpointService) {
        this.usuarioService = usuarioService;
        this.proyectoService = proyectoService;
        this.mockEndpointService = mockEndpointService;
    }

    /**
     * Página principal del dashboard.
     * Muestra estadísticas según el rol del usuario.
     */
    @GetMapping({"/", "/dashboard"})
    public String dashboard(Authentication authentication, Model model) {
        String username = authentication.getName();
        Usuario usuario = usuarioService.buscarPorUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        model.addAttribute("usuario", usuario);
        model.addAttribute("instancePort", serverPort);

        if (usuario.esAdmin()) {
            // Admin ve todo
            var proyectos = proyectoService.listarTodos();
            var mocks = mockEndpointService.listarTodos();
            var usuarios = usuarioService.listarTodos();
            model.addAttribute("proyectos", proyectos);
            model.addAttribute("mocks", mocks);
            model.addAttribute("usuarios", usuarios);
            model.addAttribute("totalProyectos", proyectos.size());
            model.addAttribute("totalMocks", mocks.size());
            model.addAttribute("totalUsuarios", usuarios.size());
        } else {
            // Usuario normal solo ve lo suyo
            var proyectos = proyectoService.listarPorUsuario(usuario.getId());
            var mocks = mockEndpointService.listarPorUsuario(usuario.getId());
            model.addAttribute("proyectos", proyectos);
            model.addAttribute("mocks", mocks);
            model.addAttribute("totalProyectos", proyectos.size());
            model.addAttribute("totalMocks", mocks.size());
        }

        return "dashboard";
    }
}
