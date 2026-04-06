package com.hospedaje.reviews.repository;

import com.hospedaje.reviews.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA repository for {@link Review} entities.
 * <p>
 * Provides built-in CRUD operations plus a custom query method
 * to retrieve all reviews associated with a specific property.
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Find all reviews for a given property, ordered by newest first.
     *
     * @param propertyId the MongoDB ObjectId of the property
     * @return list of reviews sorted by creation date (descending)
     */
    List<Review> findByPropertyIdOrderByCreatedDateDesc(String propertyId);

    /**
     * Find all reviews submitted by a specific customer.
     *
     * @param customerId the customer's user ID
     * @return list of reviews by the customer
     */
    List<Review> findByCustomerId(String customerId);
}
