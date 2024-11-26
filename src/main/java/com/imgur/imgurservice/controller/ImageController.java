package com.imgur.imgurservice.controller;

import com.imgur.imgurservice.exception.InvalidImageException;
import com.imgur.imgurservice.model.ImageResponse;
import com.imgur.imgurservice.service.ImageServiceImpl;
import com.imgur.imgurservice.util.JwtTokenManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controller for managing image-related operations.
 * Provides endpoints for uploading, deleting, and viewing images.
 */
@Slf4j
@RestController
@RequestMapping("/images")
@Tag(name = "Image Management", description = "APIs for managing image uploads, deletion, and retrieval")
public class ImageController {

    @Autowired
    private ImageServiceImpl imgService;

    @Autowired
    private JwtTokenManager jwtTokenManager;

    /**
     * Uploads an image and associates it with the authenticated user.
     *
     * @param file        the image file to upload
     * @param accessToken the JWT token for authentication
     * @return a success message
     */
    @PostMapping
    @Operation(
            summary = "Upload Image",
            description = "Uploads an image and associates it with the authenticated user.",
            requestBody = @RequestBody(
                    description = "The image file to upload",
                    content = @Content(mediaType = "multipart/form-data", schema = @Schema(type = "string", format = "binary"))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Image uploaded successfully",
                            content = @Content(schema = @Schema(implementation = ImageResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid file or token",
                            content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    public ResponseEntity<ImageResponse> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String accessToken) {
        String username = getUsernameFromToken(accessToken);
        log.info("Uploading image for user: {}", username);

        if (file == null || file.isEmpty()) {
            log.error("Uploaded file is invalid or empty.");
            throw new InvalidImageException("File is invalid or empty.");
        }

        ImageResponse imageResponse = imgService.uploadAndSaveImage(file, username);
        log.info("Image uploaded successfully for user: {}", username);

        return ResponseEntity.created(null).body(imageResponse);
    }

    /**
     * Deletes an image by its ID for the authenticated user.
     *
     * @param imageId     the ID of the image to delete
     * @param accessToken the JWT token for authentication
     * @return a success or error message
     */
    @DeleteMapping("/{imageId}")
    @Operation(
            summary = "Delete Image",
            description = "Deletes an image by its ID for the authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Image deleted successfully"),
                    @ApiResponse(responseCode = "403", description = "Access denied"),
                    @ApiResponse(responseCode = "404", description = "Image not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    public ResponseEntity<String> deleteImage(
            @PathVariable String imageId,
            @RequestHeader("Authorization") String accessToken) {
        String username = getUsernameFromToken(accessToken);
        log.info("Deleting image with ID: {} for user: {}", imageId, username);

        imgService.deleteImage(imageId, username);
        log.info("Image deleted successfully for user: {}", username);

        return ResponseEntity.ok("Image deleted successfully");
    }

    /**
     * Retrieves all images for the authenticated user.
     *
     * @param username    the username whose images are to be retrieved
     * @param accessToken the JWT token for authentication
     * @return a list of image responses
     */
    @GetMapping("/user/{username}")
    @Operation(
            summary = "Get User Images",
            description = "Retrieves all images for the authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Images retrieved successfully",
                            content = @Content(schema = @Schema(implementation = List.class))),
                    @ApiResponse(responseCode = "403", description = "Access denied"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    public ResponseEntity<List<ImageResponse>> getImagesForUser(
            @PathVariable String username,
            @RequestHeader("Authorization") String accessToken) {
        String authenticatedUsername = getUsernameFromToken(accessToken);
        if (!authenticatedUsername.equals(username)) {
            log.error("Access denied for user: {} to fetch images of: {}", authenticatedUsername, username);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        List<ImageResponse> userImages = imgService.getImagesByUsername(username);
        log.info("Retrieved images for user: {}", username);

        return ResponseEntity.ok(userImages);
    }

    /**
     * Extracts the username from a valid JWT token.
     *
     * @param accessToken the JWT token
     * @return the extractedå username
     */
    private String getUsernameFromToken(String accessToken) {
        if (accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        }
        jwtTokenManager.validateToken(accessToken);
        return jwtTokenManager.extractUsername(accessToken);
    }
}