package com.practica.clon_mocky.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controlador para la página de login.
 * Spring Security intercepta el POST /login automáticamente.
 */
@Controller
public class LoginController {

    /**
     * Muestra la página de inicio de sesión.
     *
     * @param error  true si hubo un error de autenticación
     * @param logout true si el usuario acaba de cerrar sesión
     * @param model  modelo de Thymeleaf
     * @return vista de login
     */
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {
        if (error != null) {
            model.addAttribute("error", true);
        }
        if (logout != null) {
            model.addAttribute("logout", true);
        }
        return "login";
    }
}
