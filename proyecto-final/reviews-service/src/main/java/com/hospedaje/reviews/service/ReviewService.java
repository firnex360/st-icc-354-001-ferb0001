package com.hospedaje.reviews.service;

import com.hospedaje.reviews.model.Review;
import com.hospedaje.reviews.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Business-logic layer for managing reviews and ratings.
 * <p>
 * Handles validation, persistence, and retrieval of guest reviews.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    // ───────────────────────── Queries ──────────────────────────

    /**
     * Retrieve all reviews in the system.
     */
    public List<Review> findAll() {
        log.debug("Fetching all reviews");
        return reviewRepository.findAll();
    }

    /**
     * Retrieve a single review by its ID.
     */
    public Optional<Review> findById(Long id) {
        log.debug("Looking up review id={}", id);
        return reviewRepository.findById(id);
    }

    /**
     * Retrieve all reviews for a specific property, newest first.
     *
     * @param propertyId the property's catalog ID
     * @return list of reviews ordered by creation date (descending)
     */
    public List<Review> findByPropertyId(String propertyId) {
        log.debug("Fetching reviews for propertyId={}", propertyId);
        return reviewRepository.findByPropertyIdOrderByCreatedDateDesc(propertyId);
    }

    /**
     * Calculate the average rating for a property.
     *
     * @param propertyId the property's catalog ID
     * @return the average rating, or 0.0 if no reviews exist
     */
    public double getAverageRating(String propertyId) {
        List<Review> reviews = reviewRepository.findByPropertyIdOrderByCreatedDateDesc(propertyId);
        if (reviews.isEmpty()) {
            return 0.0;
        }
        double average = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
        log.debug("Average rating for propertyId={} is {}", propertyId, average);
        return Math.round(average * 100.0) / 100.0; // Round to 2 decimals
    }

    // ───────────────────────── Commands ─────────────────────────

    /**
     * Submit a new review.
     * <p>
     * The rating is validated at the entity level (1-5 range)
     * via Bean Validation annotations on {@link Review}.
     *
     * @param review the review to persist
     * @return the saved review with generated ID and timestamp
     * @throws IllegalArgumentException if the rating is out of range
     */
    public Review create(Review review) {
        // Extra programmatic validation as a safety net
        if (review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException(
                    "Rating must be between 1 and 5 (inclusive). Got: " + review.getRating());
        }

        // Prevent duplicate reviews from the same customer for the same property
        if (reviewRepository.existsByCustomerIdAndPropertyId(
                review.getCustomerId(), review.getPropertyId())) {
            throw new IllegalArgumentException(
                    "You have already submitted a review for this property.");
        }

        review.setId(null); // Ensure JPA generates a fresh ID
        log.info("Creating review — customer={}, property={}, rating={}",
                review.getCustomerId(), review.getPropertyId(), review.getRating());
        return reviewRepository.save(review);
    }

    /**
     * Delete a review by its ID.
     */
    public void delete(Long id) {
        log.info("Deleting review id={}", id);
        reviewRepository.deleteById(id);
    }
}
