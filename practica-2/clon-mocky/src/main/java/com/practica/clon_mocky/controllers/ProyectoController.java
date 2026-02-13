package com.practica.clon_mocky.controllers;

import com.practica.clon_mocky.entities.Proyecto;
import com.practica.clon_mocky.entities.Usuario;
import com.practica.clon_mocky.services.ProyectoService;
import com.practica.clon_mocky.services.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador de gestión de proyectos.
 * Los usuarios normales solo ven sus proyectos; el admin ve todos.
 */
@Controller
@RequestMapping("/proyectos")
public class ProyectoController {

    private final ProyectoService proyectoService;
    private final UsuarioService usuarioService;

    public ProyectoController(ProyectoService proyectoService, UsuarioService usuarioService) {
        this.proyectoService = proyectoService;
        this.usuarioService = usuarioService;
    }

    /**
     * Obtiene el usuario autenticado desde la base de datos.
     */
    private Usuario getUsuarioAutenticado(Authentication auth) {
        return usuarioService.buscarPorUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    /**
     * Lista los proyectos del usuario (o todos si es admin).
     */
    @GetMapping
    public String listar(Authentication auth, Model model) {
        Usuario usuario = getUsuarioAutenticado(auth);
        model.addAttribute("usuario", usuario);

        if (usuario.esAdmin()) {
            model.addAttribute("proyectos", proyectoService.listarTodos());
        } else {
            model.addAttribute("proyectos", proyectoService.listarPorUsuario(usuario.getId()));
        }
        return "proyectos/lista";
    }

    /**
     * Muestra el formulario para crear un nuevo proyecto.
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("proyecto", new Proyecto());
        model.addAttribute("esNuevo", true);
        return "proyectos/form";
    }

    /**
     * Muestra el formulario para editar un proyecto existente.
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Authentication auth,
                                          Model model, RedirectAttributes redirect) {
        Usuario usuario = getUsuarioAutenticado(auth);

        return proyectoService.buscarPorId(id)
                .filter(p -> usuario.esAdmin() || p.getUsuario().getId().equals(usuario.getId()))
                .map(proyecto -> {
                    model.addAttribute("proyecto", proyecto);
                    model.addAttribute("esNuevo", false);
                    return "proyectos/form";
                })
                .orElseGet(() -> {
                    redirect.addFlashAttribute("error", "Proyecto no encontrado o sin permisos");
                    return "redirect:/proyectos";
                });
    }

    /**
     * Crea un nuevo proyecto asignado al usuario autenticado.
     */
    @PostMapping("/crear")
    public String crear(@RequestParam String nombre,
                        @RequestParam(required = false) String descripcion,
                        Authentication auth,
                        RedirectAttributes redirect) {
        try {
            Usuario usuario = getUsuarioAutenticado(auth);
            Proyecto proyecto = Proyecto.builder()
                    .nombre(nombre)
                    .descripcion(descripcion)
                    .build();

            proyectoService.crearProyecto(proyecto, usuario.getId());
            redirect.addFlashAttribute("success", "msg.save.success");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/proyectos";
    }

    /**
     * Actualiza un proyecto existente.
     */
    @PostMapping("/actualizar/{id}")
    public String actualizar(@PathVariable Long id,
                             @RequestParam String nombre,
                             @RequestParam(required = false) String descripcion,
                             Authentication auth,
                             RedirectAttributes redirect) {
        try {
            Usuario usuario = getUsuarioAutenticado(auth);
            // Verificar permisos
            if (!usuario.esAdmin() && !proyectoService.perteneceAUsuario(id, usuario.getId())) {
                redirect.addFlashAttribute("error", "Sin permisos para editar este proyecto");
                return "redirect:/proyectos";
            }

            Proyecto datos = Proyecto.builder()
                    .nombre(nombre)
                    .descripcion(descripcion)
                    .build();

            proyectoService.actualizarProyecto(id, datos);
            redirect.addFlashAttribute("success", "msg.save.success");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/proyectos";
    }

    /**
     * Elimina un proyecto y todos sus mocks.
     */
    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, Authentication auth, RedirectAttributes redirect) {
        try {
            Usuario usuario = getUsuarioAutenticado(auth);
            if (!usuario.esAdmin() && !proyectoService.perteneceAUsuario(id, usuario.getId())) {
                redirect.addFlashAttribute("error", "Sin permisos para eliminar este proyecto");
                return "redirect:/proyectos";
            }

            proyectoService.eliminarProyecto(id);
            redirect.addFlashAttribute("success", "msg.delete.success");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/proyectos";
    }
}
