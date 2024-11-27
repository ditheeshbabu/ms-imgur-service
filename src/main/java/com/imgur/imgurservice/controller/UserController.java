package com.imgur.imgurservice.controller;

import com.imgur.imgurservice.exception.ErrorResponse;
import com.imgur.imgurservice.model.Authentication.AuthenticationRequest;
import com.imgur.imgurservice.model.Authentication.JwtResponse;
import com.imgur.imgurservice.model.UserRequest;
import com.imgur.imgurservice.model.UserResponse;
import com.imgur.imgurservice.service.UserServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing user-related operations.
 * Provides endpoints for user registration, login, retrieval, and image association.
 */
@RestController
@RequestMapping("/users")
@Log4j2
@Validated
@Tag(name = "User Management", description = "APIs for managing user registration, login, and information retrieval")
public class UserController {

    @Autowired
    private UserServiceImpl userService;

    /**
     * Registers a new user.
     *
     * @param user the details of the user to be created
     * @return the created user's details
     */
    @PostMapping
    @Operation(
            summary = "Register User",
            description = "Registers a new user and returns the created user's details.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "User registered successfully",
                            content = @Content(schema = @Schema(implementation = UserResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRequest user) {
        UserResponse createdUser = userService.createUser(user);
        log.info("Successfully registered user: {}", createdUser.getUsername());
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    /**
     * Authenticates a user and generates a JWT token.
     *
     * @param authenticationRequest the user's login credentials
     * @return an authentication response containing the JWT token
     */
    @PostMapping("/login")
    @Operation(
            summary = "Authenticate User",
            description = "Authenticates a user with their credentials and returns a JWT token.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Authentication successful",
                            content = @Content(schema = @Schema(implementation = JwtResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody AuthenticationRequest authenticationRequest) {
        JwtResponse accessToken = userService.authorizeUser(authenticationRequest.getUsername(), authenticationRequest.getPassword());
        log.info("User {} authenticated successfully", authenticationRequest.getUsername());
        return new ResponseEntity<>(accessToken, HttpStatus.OK);
    }

    /**
     * Retrieves user basic information by their username.
     *
     * @param username the username of the user to retrieve
     * @return the user's details
     */
    @GetMapping("/{username}")
    @Operation(
            summary = "Get User by Username",
            description = "Retrieves basic information of a user by their username.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User found",
                            content = @Content(schema = @Schema(implementation = UserResponse.class))),
                    @ApiResponse(responseCode = "404", description = "User not found",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        UserResponse user = userService.getUserByName(username);
        log.info("Fetched details for user: {}", username);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    /**
     * Associates a list of images with the user profile.
     *
     * @param username the username of the user to update
     * @param imageIds the list of image IDs to associate with the user
     * @return a success message
     */
    @PutMapping("/{username}/images")
    @Operation(
            summary = "Associate Images with User",
            description = "Associates a list of image IDs with the specified user's profile.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Images associated successfully"),
                    @ApiResponse(responseCode = "404", description = "User not found",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public ResponseEntity<String> associateImagesWithUser(@PathVariable String username, @RequestBody List<String> imageIds) {
        userService.updateUserImages(username, imageIds);
        log.info("Updated images for user: {}", username);
        return new ResponseEntity<>("Images updated successfully for user: " + username, HttpStatus.OK);
    }
}