package com.imgur.imgurservice.service;

import com.imgur.imgurservice.entity.ImageEntity;
import com.imgur.imgurservice.entity.UserEntity;
import com.imgur.imgurservice.exception.AccessDeniedException;
import com.imgur.imgurservice.exception.ImageNotFoundException;
import com.imgur.imgurservice.exception.UsernameNotFoundException;
import com.imgur.imgurservice.model.ImageResponse;
import com.imgur.imgurservice.model.Imgurmodel.ImgurData;
import com.imgur.imgurservice.model.Imgurmodel.ImgurResponse;
import com.imgur.imgurservice.repository.ImageRepository;
import com.imgur.imgurservice.repository.UserRepository;
import com.imgur.imgurservice.util.JwtTokenManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the ImgService interface for managing image-related operations.
 */
@Service
@Slf4j
public class ImageServiceImpl implements ImgService {

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final JwtTokenManager jwtTokenManager;

    @Value("${imgur.uploadUrl}")
    private String uploadUrl;

    @Value("${imgur.deleteUrl}")
    private String deleteUrl;

    @Value("${imgur.clientId}")
    private String clientId;

    public ImageServiceImpl(RestTemplate restTemplate, UserRepository userRepository,
                            ImageRepository imageRepository, JwtTokenManager jwtTokenManager) {
        this.restTemplate = restTemplate;
        this.userRepository = userRepository;
        this.imageRepository = imageRepository;
        this.jwtTokenManager = jwtTokenManager;
    }

    /**
     * Uploads and saves an image associated with a user.
     *
     * @param file     the image file to upload
     * @param username the username of the user uploading the image
     * @return the image response containing the image ID and URL
     */
    @Override
    @CacheEvict(value = "imagesByUser", key = "#username")
    public ImageResponse uploadAndSaveImage(MultipartFile file, String username) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Invalid or empty file");
        }

        ImgurResponse imgurResponse = uploadImg(file);
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        ImageEntity image = new ImageEntity();
        ImgurData imgurData = imgurResponse.getData();
        String imageUrl = imgurData.getLink();
        String deleteHash = imgurData.getDeletehash();
        image.setImageUrl(imageUrl);
        image.setDeleteHash(deleteHash);
        image.setUser(user);
        imageRepository.save(image);
        log.info("Image metadata saved for user: {}", username);
        return new ImageResponse(image.getId(), imageUrl);
    }

    /**
     * Deletes an image by its ID.
     *
     * @param imageId     the ID of the image to delete
     * @param username username of the user
     */
    @Override
    @CacheEvict(value = "imagesByUser", key = "#username")
    public void deleteImage(String imageId, String username) {
        // Extract username from the token

        ImageEntity image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ImageNotFoundException("Image not found"));

        // Validate ownership
        if (!image.getUser().getUsername().equals(username)) {
            log.error("Unauthorized deletion attempt for image {} by user {}", imageId, username);
            throw new AccessDeniedException("You are not authorized to delete this image");
        }

        // Perform deletion
        deleteImageFromService(image.getDeleteHash());
        imageRepository.delete(image);
        log.info("Image deleted successfully by user {}: {}", username, imageId);
    }

    /**
     * Retrieves all images for a user.
     *
     * @param username the username of the user
     * @return a list of image responses
     */
    @Override
    @Cacheable(value = "imagesByUser", key = "#username")
    public List<ImageResponse> getImagesByUsername(String username) {

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<ImageEntity> images = imageRepository.findByUserId(user.getId());
        List<ImageResponse> responses = new ArrayList<>();

        for (ImageEntity image : images) {
            responses.add(new ImageResponse(image.getId(), image.getImageUrl()));
        }

        log.info("Images retrieved for user: {}", username);
        return responses;
    }

    /**
     * Retrieves an image by its ID.
     *
     * @param imageId the ID of the image to retrieve
     * @return the image response
     */
    @Override
    public ImageResponse getImageById(String imageId) {
        ImageEntity image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ImageNotFoundException("Image not found"));

        log.info("Retrieved image with ID: {}", imageId);
        return new ImageResponse(image.getId(), image.getImageUrl());
    }

    /**
     * Uploads an image to Imgur using the API.
     *
     * @param file the image file to upload
     * @return the Imgur response containing upload details
     */
    private ImgurResponse uploadImg(MultipartFile file) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Client-ID " + clientId);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", file.getResource());

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<ImgurResponse> response = this.restTemplate.postForEntity(uploadUrl, request, ImgurResponse.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to upload image to Imgur");
        }
    }

    /**
     * Deletes an image from Imgur using the API.
     *
     * @param deleteHash the delete hash for the image
     */
    private void deleteImageFromService(String deleteHash) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Client-ID " + clientId);

            // Ensure the base URL ends with a slash
            String url = deleteUrl.endsWith("/") ? deleteUrl : deleteUrl + "/";
            url += deleteHash; // Concatenate the delete hash

            log.info("Sending DELETE request to Imgur: {}", url);

            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<Void> response = this.restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Image deleted from Imgur: {}", deleteHash);
            } else {
                throw new RuntimeException("Failed to delete image from Imgur");
            }
        } catch (HttpClientErrorException e) {
            log.error("Imgur DELETE request failed with status {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Imgur DELETE request failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during Imgur DELETE request: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error during Imgur DELETE request");
        }
    }
}