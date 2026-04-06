package com.hospedaje.reviews;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Reviews & Ratings Microservice.
 * <p>
 * Allows guests to leave 1-5 star ratings and written comments
 * about properties after their stay. Data is persisted in PostgreSQL.
 * <p>
 * Runs on port 8084 and registers as {@code REVIEWS-SERVICE} in Eureka.
 */
@SpringBootApplication
public class ReviewsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReviewsServiceApplication.class, args);
    }
}
