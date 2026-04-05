package com.hospedaje.auth.repository;

import com.hospedaje.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link User} entities.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /** Find a user by their unique email address. */
    Optional<User> findByEmail(String email);

    /** Check whether a user with the given email already exists. */
    boolean existsByEmail(String email);
}
