package com.hotel.frontend.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "auth-service")
public interface AuthClient {

    @PostMapping("/auth/login")
    Map<String, Object> login(@RequestBody Map<String, String> credentials);
}
