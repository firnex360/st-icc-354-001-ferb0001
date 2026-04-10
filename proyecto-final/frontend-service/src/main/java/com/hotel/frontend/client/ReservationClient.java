package main.java.com.hotel.frontend.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@FeignClient(name = "reservation-service")
public interface ReservationClient {

    @GetMapping("/api/reservations/stats")
    Map<String, Object> getDashboardStats();
}
