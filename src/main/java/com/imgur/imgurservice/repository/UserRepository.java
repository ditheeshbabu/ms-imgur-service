package com.imgur.imgurservice.repository;

import com.imgur.imgurservice.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for performing database operations on the User entity.
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {

    /**
     * Finds a user by their username.
     *
     * @param username the username of the user
     * @return an Optional containing the UserEntity if found, or empty otherwise
     */
    Optional<UserEntity> findByUsername(String username);

    // Additional query methods can be added as needed.
}