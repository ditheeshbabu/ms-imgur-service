package com.imgur.imgurservice.service;

import com.imgur.imgurservice.entity.ImageEntity;
import com.imgur.imgurservice.entity.UserEntity;
import com.imgur.imgurservice.exception.AccessDeniedException;
import com.imgur.imgurservice.exception.UserAlreadyExistsException;
import com.imgur.imgurservice.exception.UsernameNotFoundException;
import com.imgur.imgurservice.model.Authentication.JwtResponse;
import com.imgur.imgurservice.model.UserRequest;
import com.imgur.imgurservice.model.UserResponse;
import com.imgur.imgurservice.repository.ImageRepository;
import com.imgur.imgurservice.repository.UserRepository;
import com.imgur.imgurservice.util.JwtTokenManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the UserService interface for managing user-related operations.
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenManager jwtTokenManager;

    public UserServiceImpl(UserRepository userRepository, ImageRepository imageRepository,
                           PasswordEncoder passwordEncoder, JwtTokenManager jwtTokenManager) {
        this.userRepository = userRepository;
        this.imageRepository = imageRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenManager = jwtTokenManager;
    }

    /**
     * Registers a new user.
     *
     * @param userRequest the user registration details
     * @return the registered user's details
     */
    @Override
    public UserResponse createUser(UserRequest userRequest) {
        if (userRepository.findByUsername(userRequest.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Username already exists");
        }

        //Create and set to entity
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(userRequest.getUsername());
        userEntity.setEmail(userRequest.getEmail());
        userEntity.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        userEntity = userRepository.save(userEntity);

        log.info("User registered successfully: {}", userRequest.getUsername());

        UserResponse response = new UserResponse();
        response.setEmail(userEntity.getEmail());
        response.setUserId(userEntity.getId());
        response.setUsername(userEntity.getUsername());
        return response;
    }

    /**
     * Retrieves a user by their username.
     *
     * @param username the username of the user to retrieve
     * @return the user's details
     */
    @Override
    @Cacheable (value = "users", key="#username")
    public UserResponse getUserByName(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UserResponse response = new UserResponse();
        response.setEmail(user.getEmail());
        response.setUsername(user.getUsername());
        response.setUserId(user.getId());
        log.info("Retrieved user: {}", username);
        return response;
    }

    /**
     * Deletes a user by their ID.
     *
     * @param userId the ID of the user to delete
     * @return a success message
     */
    @Override
    @CacheEvict(value = "users", key = "#username")
    public String deleteByUsername(String userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        userRepository.delete(user);
        log.info("Deleted user with ID: {}", userId);
        return "User deleted successfully.";
    }

    /**
     * Authenticates a user and generates a JWT token.
     *
     * @param username the username
     * @param password the password
     * @return the authentication response containing the JWT token
     */
    @Override
    public JwtResponse authorizeUser(String username, String password) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AccessDeniedException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AccessDeniedException("Invalid credentials");
        }

        String token = jwtTokenManager.generateToken(username);
        log.info("JWT token generated for user: {}", username);
        return new JwtResponse(token);
    }

    /**
     * Associates a list of image IDs with the user's profile.
     *
     * @param username the username of the user to update
     * @param imageIds the list of image IDs to associate with the user
     */
    @Override
    @CacheEvict(value = "imagesByUser", allEntries = true)
    public void updateUserImages(String username, List<String> imageIds) {
        // Fetch the user from the database
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        log.info("Found user: {}. Updating associated images...", username);

        // Fetch the Image entities for the provided IDs
        List<ImageEntity> images = imageRepository.findAllByIdIn(imageIds);

        if (images.size() != imageIds.size()) {
            log.warn("Some image IDs provided do not exist in the database.");
            throw new IllegalArgumentException("One or more image IDs are invalid.");
        }

        // Persist the association by setting the user in each ImageEntity
        for (ImageEntity image : images) {
            image.setUser(user); // Associate the user with the image
        }

        // Save the updated ImageEntity objects
        imageRepository.saveAll(images);

        log.info("Successfully updated images for user: {}", username);
    }
}