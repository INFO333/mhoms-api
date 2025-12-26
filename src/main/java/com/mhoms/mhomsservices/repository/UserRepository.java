package com.mhoms.mhomsservices.repository;

import com.mhoms.mhomsservices.model.Role;
import com.mhoms.mhomsservices.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);

    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Count users by role
     */
    long countByRole(Role role);
}