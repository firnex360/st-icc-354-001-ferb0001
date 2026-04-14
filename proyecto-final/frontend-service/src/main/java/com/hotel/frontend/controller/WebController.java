package com.hotel.frontend.controller;

import com.hotel.frontend.client.AuthClient;
import com.hotel.frontend.client.CatalogClient;
import com.hotel.frontend.client.ReservationClient;
import com.hotel.frontend.util.JwtParser;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final AuthClient authClient;
    private final CatalogClient catalogClient;
    private final ReservationClient reservationClient;

    @GetMapping({"/", "/login"})
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username, @RequestParam String password, HttpServletResponse response, Model model) {
        try {
            Map<String, String> credentials = new HashMap<>();
            credentials.put("email", username);
            credentials.put("password", password);
            
            Map<String, Object> authResponse = authClient.login(credentials);
            if (authResponse != null && authResponse.containsKey("token")) {
                String token = (String) authResponse.get("token");
                Cookie cookie = new Cookie("jwt_token", token);
                cookie.setHttpOnly(true);
                cookie.setPath("/");
                response.addCookie(cookie);
                return "redirect:/catalog";
            } else {
                model.addAttribute("error", "Invalid credentials");
                return "login";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Authentication failed: " + e.getMessage());
            return "login";
        }
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(@RequestParam String email, @RequestParam String password, HttpServletResponse response, Model model) {
        try {
            Map<String, String> credentials = new HashMap<>();
            credentials.put("email", email);
            credentials.put("password", password);

            Map<String, Object> authResponse = authClient.register(credentials);
            if (authResponse != null && authResponse.containsKey("token")) {
                String token = (String) authResponse.get("token");
                Cookie cookie = new Cookie("jwt_token", token);
                cookie.setHttpOnly(true);
                cookie.setPath("/");
                response.addCookie(cookie);
                return "redirect:/catalog";
            } else {
                model.addAttribute("error", "Registration failed");
                return "register";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt_token", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        return "redirect:/login";
    }

    @GetMapping("/catalog")
    public String getCatalog(@CookieValue(name = "jwt_token", required = false) String token, Model model) {
        String email = JwtParser.getEmail(token);
        String username = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        model.addAttribute("username", username);
        model.addAttribute("role", JwtParser.getRole(token));
        try {
            model.addAttribute("properties", catalogClient.getProperties());
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load properties: " + e.getMessage());
        }
        return "catalog";
    }

    @GetMapping("/property/{id}")
    public String getPropertyDetail(@PathVariable String id,
                                    @CookieValue(name = "jwt_token", required = false) String token,
                                    Model model) {
        String email = JwtParser.getEmail(token);
        String username = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        model.addAttribute("username", username);
        model.addAttribute("role", JwtParser.getRole(token));
        try {
            model.addAttribute("property", catalogClient.getPropertyById(id));
        } catch (Exception e) {
            model.addAttribute("error", "Property not found: " + e.getMessage());
        }
        return "property-detail";
    }

    @GetMapping("/dashboard")
    public String getDashboard(@CookieValue(name = "jwt_token", required = false) String token, Model model) {
        String role = JwtParser.getRole(token);
        if ("ROLE_CLIENT".equals(role)) {
            return "redirect:/catalog";
        }
        String email = JwtParser.getEmail(token);
        String username = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        model.addAttribute("username", username);
        model.addAttribute("role", role);
        try {
            model.addAttribute("stats", reservationClient.getDashboardStats());
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load dashboard stats: " + e.getMessage());
        }
        return "dashboard";
    }
}
