package com.hotel.frontend.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "reservation-service")
public interface ReservationClient {

    @GetMapping("/api/reservations/stats")
    Map<String, Object> getDashboardStats();

    @GetMapping("/api/reservations/customer/{customerId}")
    List<Map<String, Object>> getReservationsByCustomer(@PathVariable("customerId") String customerId);

    @PostMapping("/api/reservations")
    Map<String, Object> createReservation(@RequestBody Map<String, Object> request);

    @GetMapping("/api/reservations/{id}")
    Map<String, Object> getReservationById(@PathVariable("id") Long id);

    @GetMapping("/api/reservations")
    java.util.List<Map<String, Object>> getAllReservations();

    @PutMapping("/api/reservations/{id}/pay")
    Map<String, Object> confirmPayment(@PathVariable("id") Long id);
}
