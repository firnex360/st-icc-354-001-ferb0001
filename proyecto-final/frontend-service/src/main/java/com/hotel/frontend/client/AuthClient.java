package com.hotel.frontend.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "auth-service")
public interface AuthClient {

    @PostMapping("/auth/login")
    Map<String, Object> login(@RequestBody Map<String, String> credentials);

    @PostMapping("/auth/register")
    Map<String, Object> register(@RequestBody Map<String, String> credentials);

    /** Fetch every registered user (admin use). */
    @GetMapping("/auth/users")
    List<Map<String, Object>> getAllUsers();

    /** Fetch a single user's profile by their email address. */
    @GetMapping("/auth/users/me")
    Map<String, Object> getUserByEmail(@RequestParam("email") String email);
}
