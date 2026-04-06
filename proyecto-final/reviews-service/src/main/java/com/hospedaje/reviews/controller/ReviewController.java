package com.hospedaje.reviews.controller;

import com.hospedaje.reviews.model.Review;
import com.hospedaje.reviews.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for the Reviews & Ratings Service.
 * <p>
 * Exposes endpoints to submit reviews and retrieve ratings
 * for properties. All endpoints are prefixed with {@code /api/reviews}.
 */
@Slf4j
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // ───────────────────────── LIST ALL ─────────────────────────

    /**
     * GET /api/reviews
     * Retrieve every review in the system.
     */
    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews() {
        log.debug("GET /api/reviews — fetching all");
        return ResponseEntity.ok(reviewService.findAll());
    }

    // ───────────────────────── GET BY ID ────────────────────────

    /**
     * GET /api/reviews/{id}
     * Retrieve a single review by its primary key.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Review> getReviewById(@PathVariable Long id) {
        log.debug("GET /api/reviews/{}", id);
        return reviewService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ───────────────────── GET BY PROPERTY ──────────────────────

    /**
     * GET /api/reviews/property/{propertyId}
     * Retrieve all reviews for a specific property, newest first.
     */
    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<Review>> getReviewsByProperty(
            @PathVariable String propertyId) {
        log.debug("GET /api/reviews/property/{}", propertyId);
        return ResponseEntity.ok(reviewService.findByPropertyId(propertyId));
    }

    // ───────────────── GET AVERAGE RATING ──────────────────────

    /**
     * GET /api/reviews/property/{propertyId}/average
     * Returns the average star rating and total review count
     * for a specific property.
     */
    @GetMapping("/property/{propertyId}/average")
    public ResponseEntity<Map<String, Object>> getAverageRating(
            @PathVariable String propertyId) {
        log.debug("GET /api/reviews/property/{}/average", propertyId);

        double average = reviewService.getAverageRating(propertyId);
        List<Review> reviews = reviewService.findByPropertyId(propertyId);

        Map<String, Object> result = new HashMap<>();
        result.put("propertyId", propertyId);
        result.put("averageRating", average);
        result.put("totalReviews", reviews.size());

        return ResponseEntity.ok(result);
    }

    // ───────────────────────── CREATE ──────────────────────────

    /**
     * POST /api/reviews
     * Submit a new review for a property.
     * <p>
     * The rating must be between 1 and 5 (inclusive).
     * Validated via {@code @Valid} and Bean Validation on the entity.
     */
    @PostMapping
    public ResponseEntity<Review> createReview(@Valid @RequestBody Review review) {
        log.debug("POST /api/reviews — customer={}, property={}, rating={}",
                review.getCustomerId(), review.getPropertyId(), review.getRating());
        Review created = reviewService.create(review);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ───────────────────────── DELETE ──────────────────────────

    /**
     * DELETE /api/reviews/{id}
     * Remove a review from the system.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        log.debug("DELETE /api/reviews/{}", id);
        reviewService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ───────────────── EXCEPTION HANDLER ───────────────────────

    /**
     * Handle validation and business-rule errors for this controller.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("status", 400);
        body.put("error", "Bad Request");
        body.put("message", ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }
}
