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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            List<Map<String, Object>> properties = catalogClient.getProperties();

            if (properties != null) {
                if (place != null && !place.trim().isEmpty()) {
                    String p = place.toLowerCase();
                    properties = properties.stream()
                        .filter(prop -> {
                            String name = prop.get("name") != null ? String.valueOf(prop.get("name")).toLowerCase() : "";
                            String loc = prop.get("location") != null ? String.valueOf(prop.get("location")).toLowerCase() : "";
                            return name.contains(p) || loc.contains(p);
                        })
                        .collect(Collectors.toList());
                }
                if (minPrice != null) {
                    properties = properties.stream()
                        .filter(prop -> prop.get("pricePerNight") != null && Double.parseDouble(String.valueOf(prop.get("pricePerNight"))) >= minPrice)
                        .collect(Collectors.toList());
                }
                if (maxPrice != null) {
                    properties = properties.stream()
                        .filter(prop -> prop.get("pricePerNight") != null && Double.parseDouble(String.valueOf(prop.get("pricePerNight"))) <= maxPrice)
                        .collect(Collectors.toList());
                }
                if (minRating != null && minRating > 0) {
                    properties = properties.stream()
                        .filter(prop -> prop.get("rating") == null || Double.parseDouble(String.valueOf(prop.get("rating"))) >= minRating)
                        .collect(Collectors.toList());
                }
            }

            model.addAttribute("properties", properties);
            model.addAttribute("place", place);
            model.addAttribute("minPrice", minPrice);
            model.addAttribute("maxPrice", maxPrice);
            model.addAttribute("minRating", minRating);
            model.addAttribute("checkInDate", checkInDate);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load properties: " + e.getMessage());
        }
        return "catalog";
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
