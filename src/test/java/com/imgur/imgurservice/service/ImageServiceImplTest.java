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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * JUnit test class for the ImageServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
public class ImageServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private JwtTokenManager jwtTokenManager;

    private ImageServiceImpl imageService;

    // Set up @Value fields and inject dependencies
    @BeforeEach
    public void setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);

        // Manually create instance of ImageServiceImpl with all mocked dependencies
        imageService = new ImageServiceImpl(restTemplate, userRepository, imageRepository, jwtTokenManager);

        // Use ReflectionTestUtils to set private @Value fields
        ReflectionTestUtils.setField(imageService, "uploadUrl", "http://mock-upload-url");
        ReflectionTestUtils.setField(imageService, "deleteUrl", "http://mock-delete-url");
        ReflectionTestUtils.setField(imageService, "clientId", "mock-client-id");
    }

    @Test
    public void testUploadAndSaveImage_Success() {
        // Arrange
        String username = "testuser";
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getResource()).thenReturn(null); // Adjust as needed

        UserEntity userEntity = new UserEntity();
        userEntity.setId("userId");
        userEntity.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(userEntity));

        ImgurResponse imgurResponse = new ImgurResponse();
        ImgurData imgurData = new ImgurData();
        imgurData.setLink("http://image-link.com");
        imgurData.setDeletehash("deleteHash");
        imgurResponse.setData(imgurData);

        // Mock RestTemplate POST call
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(ImgurResponse.class)))
                .thenReturn(new ResponseEntity<>(imgurResponse, HttpStatus.OK));

        // Act
        ImageResponse imageResponse = imageService.uploadAndSaveImage(mockFile, username);

        // Assert
        assertNotNull(imageResponse);
        assertEquals("http://image-link.com", imageResponse.getImageUrl());

        // Verify that imageRepository.save was called
        verify(imageRepository, times(1)).save(any(ImageEntity.class));
    }

    @Test
    public void testUploadAndSaveImage_FileIsEmpty() {
        // Arrange
        String username = "testuser";
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> imageService.uploadAndSaveImage(mockFile, username));
        assertEquals("Invalid or empty file", exception.getMessage());
    }

    @Test
    public void testUploadAndSaveImage_UserNotFound() {
        // Arrange
        String username = "testuser";
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Mock RestTemplate POST call to prevent RuntimeException
        ImgurResponse imgurResponse = new ImgurResponse();
        ImgurData imgurData = new ImgurData();
        imgurData.setLink("http://image-link.com");
        imgurData.setDeletehash("deleteHash");
        imgurResponse.setData(imgurData);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(ImgurResponse.class)))
                .thenReturn(new ResponseEntity<>(imgurResponse, HttpStatus.OK));

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> imageService.uploadAndSaveImage(mockFile, username));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    public void testDeleteImage_Success() {
        // Arrange
        String imageId = "imageId";
        String accessToken = "mockAccessToken";
        String username = "testuser";

        UserEntity user = new UserEntity();
        user.setUsername(username);

        ImageEntity imageEntity = new ImageEntity();
        imageEntity.setId(imageId);
        imageEntity.setDeleteHash("deleteHash");
        imageEntity.setUser(user);

        when(imageRepository.findById(imageId)).thenReturn(Optional.of(imageEntity));

        // Mock RestTemplate DELETE call
        when(restTemplate.exchange(anyString(), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // Act
        imageService.deleteImage(imageId, username);

        // Assert
        verify(imageRepository, times(1)).delete(imageEntity);
    }

    @Test
    public void testDeleteImage_ImageNotFound() {
        // Arrange
        String imageId = "imageId";
        String accessToken = "mockAccessToken";

        when(imageRepository.findById(imageId)).thenReturn(Optional.empty());

        // Act & Assert
        ImageNotFoundException exception = assertThrows(ImageNotFoundException.class,
                () -> imageService.deleteImage(imageId, accessToken));
        assertEquals("Image not found", exception.getMessage());
    }

    @Test
    public void testDeleteImage_AccessDenied() {
        // Arrange
        String imageId = "imageId";
        String accessToken = "mockAccessToken";
        String username = "testuser";
        String otherUsername = "otheruser";

        UserEntity otherUser = new UserEntity();
        otherUser.setUsername(otherUsername);

        ImageEntity imageEntity = new ImageEntity();
        imageEntity.setId(imageId);
        imageEntity.setDeleteHash("deleteHash");
        imageEntity.setUser(otherUser);

        when(imageRepository.findById(imageId)).thenReturn(Optional.of(imageEntity));

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> imageService.deleteImage(imageId, accessToken));
        assertEquals("You are not authorized to delete this image", exception.getMessage());
    }

    @Test
    public void testGetImagesByUsername_Success() {
        // Arrange
        String username = "testuser";
        UserEntity userEntity = new UserEntity();
        userEntity.setId("userId");
        userEntity.setUsername(username);

        ImageEntity image1 = new ImageEntity();
        image1.setId("imageId1");
        image1.setImageUrl("http://image1-url.com");

        ImageEntity image2 = new ImageEntity();
        image2.setId("imageId2");
        image2.setImageUrl("http://image2-url.com");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(userEntity));
        when(imageRepository.findByUserId("userId")).thenReturn(Arrays.asList(image1, image2));

        // Act
        List<ImageResponse> images = imageService.getImagesByUsername(username);

        // Assert
        assertNotNull(images);
        assertEquals(2, images.size());
        assertEquals("imageId1", images.get(0).getImageId());
        assertEquals("http://image1-url.com", images.get(0).getImageUrl());
    }

    @Test
    public void testGetImagesByUsername_UserNotFound() {
        // Arrange
        String username = "testuser";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> imageService.getImagesByUsername(username));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    public void testGetImageById_Success() {
        // Arrange
        String imageId = "imageId";
        ImageEntity imageEntity = new ImageEntity();
        imageEntity.setId(imageId);
        imageEntity.setImageUrl("http://image-url.com");

        when(imageRepository.findById(imageId)).thenReturn(Optional.of(imageEntity));

        // Act
        ImageResponse imageResponse = imageService.getImageById(imageId);

        // Assert
        assertNotNull(imageResponse);
        assertEquals(imageId, imageResponse.getImageId());
        assertEquals("http://image-url.com", imageResponse.getImageUrl());
    }

    @Test
    public void testGetImageById_ImageNotFound() {
        // Arrange
        String imageId = "imageId";

        when(imageRepository.findById(imageId)).thenReturn(Optional.empty());

        // Act & Assert
        ImageNotFoundException exception = assertThrows(ImageNotFoundException.class,
                () -> imageService.getImageById(imageId));
        assertEquals("Image not found", exception.getMessage());
    }
}