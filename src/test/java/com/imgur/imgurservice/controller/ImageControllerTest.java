//package com.imgur.imgurservice.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.imgur.imgurservice.exception.AccessDeniedException;
//import com.imgur.imgurservice.exception.ImageNotFoundException;
//import com.imgur.imgurservice.exception.InvalidImageException;
//import com.imgur.imgurservice.model.ImageResponse;
//import com.imgur.imgurservice.service.ImageServiceImpl;
//import com.imgur.imgurservice.util.JwtTokenManager;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.Arrays;
//import java.util.List;
//
//import static org.hamcrest.Matchers.hasSize;
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(ImageController.class)
//@ExtendWith(MockitoExtension.class)
//public class ImageControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private ImageServiceImpl imgService;
//
//    @MockBean
//    private JwtTokenManager jwtTokenManager;
//
//    private String validToken;
//    private String invalidToken;
//    private String username;
//
//    @BeforeEach
//    public void setUp() {
//        validToken = "Bearer valid-token";
//        invalidToken = "Bearer invalid-token";
//        username = "testuser";
//    }
//
//    @Test
//    public void testUploadImage_Success() throws Exception {
//        // Arrange
//        MockMultipartFile file = new MockMultipartFile(
//                "file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test image content".getBytes());
//
//        ImageResponse imageResponse = new ImageResponse("imageId", "http://image-link.com");
//
//        // Simulate valid token
//        doNothing().when(jwtTokenManager).validateToken(anyString());
//        when(jwtTokenManager.extractUsername(anyString())).thenReturn(username);
//        when(imgService.uploadAndSaveImage(any(MultipartFile.class), eq(username))).thenReturn(imageResponse);
//
//        // Act & Assert
//        mockMvc.perform(MockMvcRequestBuilders.multipart("/images")
//                        .file(file)
//                        .header("Authorization", validToken))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.id").value("imageId"))
//                .andExpect(jsonPath("$.imageUrl").value("http://image-link.com"));
//    }
//
//    @Test
//    public void testUploadImage_InvalidFile() throws Exception {
//        // Arrange
//        MockMultipartFile file = new MockMultipartFile(
//                "file", "", MediaType.IMAGE_JPEG_VALUE, new byte[0]);
//
//        // Simulate valid token
//        doNothing().when(jwtTokenManager).validateToken(anyString());
//        when(jwtTokenManager.extractUsername(anyString())).thenReturn(username);
//
//        // Act & Assert
//        mockMvc.perform(MockMvcRequestBuilders.multipart("/images")
//                        .file(file)
//                        .header("Authorization", validToken))
//                .andExpect(status().isBadRequest())
//                .andExpect(result -> assertTrue(result.getResolvedException() instanceof InvalidImageException))
//                .andExpect(result -> assertEquals("File is invalid or empty.", result.getResolvedException().getMessage()));
//    }
//
//    @Test
//    public void testUploadImage_InvalidToken() throws Exception {
//        // Arrange
//        MockMultipartFile file = new MockMultipartFile(
//                "file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test image content".getBytes());
//
//        // Simulate invalid token
//        doThrow(new BadCredentialsException("Invalid token")).when(jwtTokenManager).validateToken(anyString());
//
//        // Act & Assert
//        mockMvc.perform(MockMvcRequestBuilders.multipart("/images")
//                        .file(file)
//                        .header("Authorization", invalidToken))
//                .andExpect(status().isUnauthorized());
//    }
//
//    @Test
//    public void testDeleteImage_Success() throws Exception {
//        // Arrange
//        String imageId = "imageId";
//
//        // Simulate valid token
//        doNothing().when(jwtTokenManager).validateToken(anyString());
//        when(jwtTokenManager.extractUsername(anyString())).thenReturn(username);
//
//        // Act & Assert
//        mockMvc.perform(MockMvcRequestBuilders.delete("/images/{imageId}", imageId)
//                        .header("Authorization", validToken))
//                .andExpect(status().isOk())
//                .andExpect(content().string("Image deleted successfully"));
//    }
//
//    @Test
//    public void testDeleteImage_ImageNotFound() throws Exception {
//        // Arrange
//        String imageId = "nonExistentImageId";
//
//        // Simulate valid token
//        doNothing().when(jwtTokenManager).validateToken(anyString());
//        when(jwtTokenManager.extractUsername(anyString())).thenReturn(username);
//
//        // Simulate ImageNotFoundException
//        doThrow(new ImageNotFoundException("Image not found"))
//                .when(imgService).deleteImage(eq(imageId), eq(username));
//
//        // Act & Assert
//        mockMvc.perform(MockMvcRequestBuilders.delete("/images/{imageId}", imageId)
//                        .header("Authorization", validToken))
//                .andExpect(status().isNotFound())
//                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ImageNotFoundException))
//                .andExpect(result -> assertEquals("Image not found", result.getResolvedException().getMessage()));
//    }
//
//    @Test
//    public void testDeleteImage_AccessDenied() throws Exception {
//        // Arrange
//        String imageId = "imageId";
//
//        // Simulate valid token
//        doNothing().when(jwtTokenManager).validateToken(anyString());
//        when(jwtTokenManager.extractUsername(anyString())).thenReturn(username);
//
//        // Simulate AccessDeniedException
//        doThrow(new AccessDeniedException("You are not authorized to delete this image"))
//                .when(imgService).deleteImage(eq(imageId), eq(username));
//
//        // Act & Assert
//        mockMvc.perform(MockMvcRequestBuilders.delete("/images/{imageId}", imageId)
//                        .header("Authorization", validToken))
//                .andExpect(status().isUnauthorized())
//                .andExpect(result -> assertTrue(result.getResolvedException() instanceof AccessDeniedException))
//                .andExpect(result -> assertEquals("You are not authorized to delete this image", result.getResolvedException().getMessage()));
//    }
//
//    @Test
//    public void testGetImagesForUser_Success() throws Exception {
//        // Arrange
//        List<ImageResponse> images = Arrays.asList(
//                new ImageResponse("imageId1", "http://image1-url.com"),
//                new ImageResponse("imageId2", "http://image2-url.com")
//        );
//
//        // Simulate valid token
//        doNothing().when(jwtTokenManager).validateToken(anyString());
//        when(jwtTokenManager.extractUsername(anyString())).thenReturn(username);
//        when(imgService.getImagesByUsername(eq(username))).thenReturn(images);
//
//        // Act & Assert
//        mockMvc.perform(MockMvcRequestBuilders.get("/images/user/{username}", username)
//                        .header("Authorization", validToken))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$", hasSize(2)))
//                .andExpect(jsonPath("$[0].id").value("imageId1"))
//                .andExpect(jsonPath("$[0].imageUrl").value("http://image1-url.com"))
//                .andExpect(jsonPath("$[1].id").value("imageId2"))
//                .andExpect(jsonPath("$[1].imageUrl").value("http://image2-url.com"));
//    }
//
//    @Test
//    public void testGetImagesForUser_UnauthorizedAccess() throws Exception {
//        // Arrange
//        String otherUsername = "otheruser";
//
//        // Simulate valid token
//        doNothing().when(jwtTokenManager).validateToken(anyString());
//        when(jwtTokenManager.extractUsername(anyString())).thenReturn(username);
//
//        // Act & Assert
//        mockMvc.perform(MockMvcRequestBuilders.get("/images/user/{username}", otherUsername)
//                        .header("Authorization", validToken))
//                .andExpect(status().isForbidden());
//    }
//
//    @Test
//    public void testGetImagesForUser_InvalidToken() throws Exception {
//        // Arrange
//        String otherUsername = "otheruser";
//
//        // Simulate invalid token
//        doThrow(new BadCredentialsException("Invalid token")).when(jwtTokenManager).validateToken(anyString());
//
//        // Act & Assert
//        mockMvc.perform(MockMvcRequestBuilders.get("/images/user/{username}", otherUsername)
//                        .header("Authorization", invalidToken))
//                .andExpect(status().isUnauthorized());
//    }
//}