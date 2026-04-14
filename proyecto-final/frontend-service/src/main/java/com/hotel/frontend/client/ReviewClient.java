package com.hotel.frontend.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * Feign client for the Reviews & Ratings microservice.
 * Routed through the API Gateway (lb://REVIEWS-SERVICE).
 */
@FeignClient(name = "reviews-service")
public interface ReviewClient {

    /** Fetch all reviews for a specific property, newest first. */
    @GetMapping("/api/reviews/property/{propertyId}")
    List<Map<String, Object>> getReviewsByProperty(@PathVariable("propertyId") String propertyId);

    /** Fetch the average rating and total review count for a property. */
    @GetMapping("/api/reviews/property/{propertyId}/average")
    Map<String, Object> getPropertyAverageRating(@PathVariable("propertyId") String propertyId);

    /** Fetch every review in the system (used to compute catalog averages in one call). */
    @GetMapping("/api/reviews")
    List<Map<String, Object>> getAllReviews();

    /** Submit a new star review for a property. */
    @PostMapping("/api/reviews")
    Map<String, Object> submitReview(@RequestBody Map<String, Object> review);
}