package com.practica.clon_mocky.controllers;

import com.practica.clon_mocky.entities.Usuario;
import com.practica.clon_mocky.services.MockEndpointService;
import com.practica.clon_mocky.services.ProyectoService;
import com.practica.clon_mocky.services.UsuarioService;
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

        if (usuario.esAdmin()) {
            // Admin ve todo
            model.addAttribute("proyectos", proyectoService.listarTodos());
            model.addAttribute("mocks", mockEndpointService.listarTodos());
            model.addAttribute("usuarios", usuarioService.listarTodos());
            model.addAttribute("totalProyectos", proyectoService.listarTodos().size());
            model.addAttribute("totalMocks", mockEndpointService.listarTodos().size());
            model.addAttribute("totalUsuarios", usuarioService.listarTodos().size());
        } else {
            // Usuario normal solo ve lo suyo
            model.addAttribute("proyectos", proyectoService.listarPorUsuario(usuario.getId()));
            model.addAttribute("mocks", mockEndpointService.listarPorUsuario(usuario.getId()));
            model.addAttribute("totalProyectos", proyectoService.listarPorUsuario(usuario.getId()).size());
            model.addAttribute("totalMocks", mockEndpointService.listarPorUsuario(usuario.getId()).size());
        }

        return "dashboard";
    }
}
