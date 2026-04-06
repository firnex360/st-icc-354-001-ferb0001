package com.hospedaje.reviews.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * JPA entity representing a guest review for a property.
 * <p>
 * Each review contains a 1-5 star rating and an optional
 * written comment. Linked to a property and customer by their IDs.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reviews")
public class Review {

    /** Auto-generated primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference to the property being reviewed.
     * This is the MongoDB ObjectId from the Catalog Service.
     */
    @NotBlank(message = "Property ID is required")
    @Column(nullable = false)
    private String propertyId;

    /**
     * Reference to the customer who wrote the review.
     * Maps to the user ID from the Auth Service.
     */
    @NotBlank(message = "Customer ID is required")
    @Column(nullable = false)
    private String customerId;

    /**
     * Star rating from 1 (worst) to 5 (best).
     * Validated at both entity and controller level.
     */
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    @Column(nullable = false)
    private Integer rating;

    /** Optional written comment about the stay. */
    @Column(length = 2000)
    private String comment;

    /** Timestamp when the review was submitted. */
    @Column(updatable = false)
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
    }
}
