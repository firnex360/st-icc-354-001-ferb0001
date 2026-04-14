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

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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
    public String getCatalog(@CookieValue(name = "jwt_token", required = false) String token, 
            @RequestParam(required = false) String place,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) String checkInDate,
            Model model) {
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

    // ─── Reservation flow ────────────────────────────────────────────

    @GetMapping("/reserve/{propertyId}")
    public String reservePage(@PathVariable String propertyId,
                              @CookieValue(name = "jwt_token", required = false) String token,
                              Model model) {
        String email    = JwtParser.getEmail(token);
        String username = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        model.addAttribute("username", username);
        model.addAttribute("role", JwtParser.getRole(token));
        try {
            model.addAttribute("property", catalogClient.getPropertyById(propertyId));
        } catch (Exception e) {
            model.addAttribute("error", "Property not found: " + e.getMessage());
        }
        return "reserve";
    }

    @PostMapping("/reserve/{propertyId}")
    public String submitReservation(@PathVariable String propertyId,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
                                    @RequestParam int numberOfGuests,
                                    @CookieValue(name = "jwt_token", required = false) String token,
                                    Model model) {
        String email = JwtParser.getEmail(token);
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("customerId",     email);
            request.put("propertyId",     propertyId);
            request.put("checkInDate",    checkInDate.toString());
            request.put("checkOutDate",   checkOutDate.toString());
            request.put("numberOfGuests", numberOfGuests);

            Map<String, Object> reservation = reservationClient.createReservation(request);
            Number id = (Number) reservation.get("id");
            return "redirect:/payment/" + id.longValue();
        } catch (Exception e) {
            String username = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
            model.addAttribute("username", username);
            model.addAttribute("role", JwtParser.getRole(token));
            model.addAttribute("error", "Could not create reservation: " + e.getMessage());
            try { model.addAttribute("property", catalogClient.getPropertyById(propertyId)); } catch (Exception ignored) {}
            return "reserve";
        }
    }

    // ─── Payment flow ─────────────────────────────────────────────────

    @GetMapping("/payment/{reservationId}")
    public String paymentPage(@PathVariable Long reservationId,
                              @CookieValue(name = "jwt_token", required = false) String token,
                              Model model) {
        String email    = JwtParser.getEmail(token);
        String username = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        model.addAttribute("username", username);
        model.addAttribute("role", JwtParser.getRole(token));
        try {
            Map<String, Object> reservation = reservationClient.getReservationById(reservationId);
            model.addAttribute("reservation", reservation);

            // Compute nights for display
            LocalDate checkIn  = LocalDate.parse(reservation.get("checkInDate").toString());
            LocalDate checkOut = LocalDate.parse(reservation.get("checkOutDate").toString());
            long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
            double subtotal  = ((Number) reservation.get("totalPrice")).doubleValue();
            double taxAmount = subtotal * 0.18;
            double total     = subtotal + taxAmount;

            model.addAttribute("nights",   nights);
            model.addAttribute("subtotal", subtotal);
            model.addAttribute("tax",      taxAmount);
            model.addAttribute("total",    total);

            // Fetch property name
            try {
                Map<String, Object> property = catalogClient.getPropertyById(reservation.get("propertyId").toString());
                model.addAttribute("propertyName", property.get("name"));
            } catch (Exception ignored) {
                model.addAttribute("propertyName", reservation.get("propertyId"));
            }
        } catch (Exception e) {
            model.addAttribute("error", "Reservation not found: " + e.getMessage());
        }
        return "payment";
    }

    @PostMapping("/payment/{reservationId}/confirm")
    public String confirmPayment(@PathVariable Long reservationId,
                                 @CookieValue(name = "jwt_token", required = false) String token,
                                 Model model) {
        try {
            reservationClient.confirmPayment(reservationId);
            return "redirect:/payment/" + reservationId + "/success";
        } catch (Exception e) {
            String email    = JwtParser.getEmail(token);
            String username = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
            model.addAttribute("username", username);
            model.addAttribute("role", JwtParser.getRole(token));
            model.addAttribute("error", "Payment failed: " + e.getMessage());
            return "payment";
        }
    }

    @GetMapping("/payment/{reservationId}/success")
    public String paymentSuccess(@PathVariable Long reservationId,
                                 @CookieValue(name = "jwt_token", required = false) String token,
                                 Model model) {
        String email    = JwtParser.getEmail(token);
        String username = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        model.addAttribute("username", username);
        model.addAttribute("role", JwtParser.getRole(token));
        try {
            Map<String, Object> reservation = reservationClient.getReservationById(reservationId);
            model.addAttribute("reservation", reservation);

            LocalDate checkIn  = LocalDate.parse(reservation.get("checkInDate").toString());
            LocalDate checkOut = LocalDate.parse(reservation.get("checkOutDate").toString());
            long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
            double subtotal  = ((Number) reservation.get("totalPrice")).doubleValue();
            double taxAmount = subtotal * 0.18;
            double total     = subtotal + taxAmount;

            model.addAttribute("nights",   nights);
            model.addAttribute("subtotal", subtotal);
            model.addAttribute("tax",      taxAmount);
            model.addAttribute("total",    total);

            try {
                Map<String, Object> property = catalogClient.getPropertyById(reservation.get("propertyId").toString());
                model.addAttribute("propertyName", property.get("name"));
            } catch (Exception ignored) {
                model.addAttribute("propertyName", reservation.get("propertyId"));
            }
        } catch (Exception e) {
            model.addAttribute("error", "Could not load reservation details.");
        }
        return "payment-success";
    }

    // ─── My Reservations ─────────────────────────────────────────────

    @GetMapping("/my-reservations")
    public String myReservations(@CookieValue(name = "jwt_token", required = false) String token, Model model) {
        String email    = JwtParser.getEmail(token);
        String username = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        model.addAttribute("username", username);
        model.addAttribute("role", JwtParser.getRole(token));

        try {
            List<Map<String, Object>> reservations = reservationClient.getReservationsByCustomer(email);

            List<Map<String, Object>> upcoming = new ArrayList<>();
            List<Map<String, Object>> past     = new ArrayList<>();

            LocalDate today = LocalDate.now();

            if (reservations != null) {
                for (Map<String, Object> res : reservations) {
                    // Make a mutable copy so we can inject enriched fields
                    Map<String, Object> enriched = new HashMap<>(res);

                    // Enrich with property name and first image from catalog
                    try {
                        String propertyId = String.valueOf(res.get("propertyId"));
                        Map<String, Object> property = catalogClient.getPropertyById(propertyId);
                        if (property != null) {
                            enriched.put("propertyName", property.get("name"));
                            Object imageUrls = property.get("imageUrls");
                            if (imageUrls instanceof List<?> imgs && !((List<?>) imgs).isEmpty()) {
                                enriched.put("propertyImage", imgs.get(0));
                            }
                        }
                    } catch (Exception ignored) {}

                    // Classify as upcoming or past
                    boolean isCancelled = "CANCELLED".equals(String.valueOf(res.get("status")));
                    LocalDate checkOut  = null;
                    try {
                        checkOut = LocalDate.parse(String.valueOf(res.get("checkOutDate")));
                    } catch (Exception ignored) {}

                    if (isCancelled || (checkOut != null && checkOut.isBefore(today))) {
                        past.add(enriched);
                    } else {
                        upcoming.add(enriched);
                    }
                }
            }

            model.addAttribute("upcomingReservations", upcoming);
            model.addAttribute("pastReservations",     past);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load reservations: " + e.getMessage());
        }

        return "my-reservations";
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
