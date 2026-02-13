package com.practica.clon_mocky.controllers;

import com.practica.clon_mocky.entities.Usuario;
import com.practica.clon_mocky.enums.Rol;
import com.practica.clon_mocky.services.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

/**
 * Controlador de administración de usuarios.
 * Solo accesible por ROLE_ADMIN (protegido en SecurityConfig con /admin/**).
 */
@Controller
@RequestMapping("/admin/usuarios")
public class AdminUsuarioController {

    private final UsuarioService usuarioService;

    public AdminUsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Lista todos los usuarios del sistema.
     */
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "admin/usuarios";
    }

    /**
     * Muestra el formulario para crear un nuevo usuario.
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("roles", Rol.values());
        model.addAttribute("esNuevo", true);
        return "admin/usuario-form";
    }

    /**
     * Muestra el formulario para editar un usuario existente.
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        return usuarioService.buscarPorId(id)
                .map(usuario -> {
                    model.addAttribute("usuario", usuario);
                    model.addAttribute("roles", Rol.values());
                    model.addAttribute("esNuevo", false);
                    return "admin/usuario-form";
                })
                .orElseGet(() -> {
                    redirect.addFlashAttribute("error", "Usuario no encontrado");
                    return "redirect:/admin/usuarios";
                });
    }

    /**
     * Crea un nuevo usuario.
     */
    @PostMapping("/crear")
    public String crear(@RequestParam String username,
                        @RequestParam String password,
                        @RequestParam String nombre,
                        @RequestParam(required = false) List<String> roles,
                        RedirectAttributes redirect) {
        try {
            List<Rol> rolesEnum = new ArrayList<>();
            if (roles != null) {
                for (String r : roles) {
                    rolesEnum.add(Rol.valueOf(r));
                }
            }

            Usuario usuario = Usuario.builder()
                    .username(username)
                    .password(password)
                    .nombre(nombre)
                    .activo(true)
                    .roles(rolesEnum)
                    .build();

            usuarioService.crearUsuario(usuario);
            redirect.addFlashAttribute("success", "msg.save.success");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    /**
     * Actualiza un usuario existente.
     */
    @PostMapping("/actualizar/{id}")
    public String actualizar(@PathVariable Long id,
                             @RequestParam String nombre,
                             @RequestParam(required = false) String password,
                             @RequestParam(required = false) List<String> roles,
                             @RequestParam(required = false) boolean activo,
                             RedirectAttributes redirect) {
        try {
            List<Rol> rolesEnum = new ArrayList<>();
            if (roles != null) {
                for (String r : roles) {
                    rolesEnum.add(Rol.valueOf(r));
                }
            }

            Usuario datos = Usuario.builder()
                    .nombre(nombre)
                    .password(password)
                    .activo(activo)
                    .roles(rolesEnum)
                    .build();

            usuarioService.actualizarUsuario(id, datos);
            redirect.addFlashAttribute("success", "msg.save.success");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    /**
     * Elimina un usuario.
     */
    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            usuarioService.eliminarUsuario(id);
            redirect.addFlashAttribute("success", "msg.delete.success");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }
}
