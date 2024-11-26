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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * JUnit test class for the UserServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenManager jwtTokenManager;

    private UserServiceImpl userService;

    @BeforeEach
    public void setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);

        // Create instance of UserServiceImpl with mocked dependencies
        userService = new UserServiceImpl(userRepository, imageRepository, passwordEncoder, jwtTokenManager);
    }

    @Test
    public void testCreateUser_Success() {
        // Arrange
        UserRequest userRequest = new UserRequest();
        userRequest.setUsername("testuser");
        userRequest.setPassword("password");
        userRequest.setEmail("test@example.com");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        UserEntity savedUser = new UserEntity();
        savedUser.setId("userId");
        savedUser.setUsername("testuser");
        savedUser.setPassword("encodedPassword");
        savedUser.setEmail("test@example.com");

        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);

        // Act
        UserResponse response = userService.createUser(userRequest);

        // Assert
        assertNotNull(response);
        assertEquals("userId", response.getUserId());
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());

        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    public void testCreateUser_UserAlreadyExists() {
        // Arrange
        UserRequest userRequest = new UserRequest();
        userRequest.setUsername("existingUser");
        userRequest.setPassword("password");

        when(userRepository.findByUsername("existingUser")).thenReturn(Optional.of(new UserEntity()));

        // Act & Assert
        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () -> {
            userService.createUser(userRequest);
        });

        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    public void testGetUserByName_Success() {
        // Arrange
        String username = "testuser";
        UserEntity userEntity = new UserEntity();
        userEntity.setId("userId");
        userEntity.setUsername(username);
        userEntity.setEmail("test@example.com");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(userEntity));

        // Act
        UserResponse response = userService.getUserByName(username);

        // Assert
        assertNotNull(response);
        assertEquals("userId", response.getUserId());
        assertEquals(username, response.getUsername());
        assertEquals("test@example.com", response.getEmail());
    }

    @Test
    public void testGetUserByName_UserNotFound() {
        // Arrange
        String username = "nonExistingUser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userService.getUserByName(username);
        });

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    public void testDeleteByUsername_Success() {
        // Arrange
        String userId = "userId";
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setUsername("testuser");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

        // Act
        String result = userService.deleteByUsername(userId);

        // Assert
        assertEquals("User deleted successfully.", result);
        verify(userRepository, times(1)).delete(userEntity);
    }

    @Test
    public void testDeleteByUsername_UserNotFound() {
        // Arrange
        String userId = "nonExistingUserId";
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userService.deleteByUsername(userId);
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, never()).delete(any(UserEntity.class));
    }

    @Test
    public void testAuthorizeUser_Success() {
        // Arrange
        String username = "testuser";
        String password = "password";
        String encodedPassword = "encodedPassword";

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(username);
        userEntity.setPassword(encodedPassword);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        when(jwtTokenManager.generateToken(username)).thenReturn("jwtToken");

        // Act
        JwtResponse jwtResponse = userService.authorizeUser(username, password);

        // Assert
        assertNotNull(jwtResponse);
        assertEquals("jwtToken", jwtResponse.getJwt());
    }

    @Test
    public void testAuthorizeUser_InvalidCredentials_WrongPassword() {
        // Arrange
        String username = "testuser";
        String password = "wrongPassword";
        String encodedPassword = "encodedPassword";

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(username);
        userEntity.setPassword(encodedPassword);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            userService.authorizeUser(username, password);
        });

        assertEquals("Invalid credentials", exception.getMessage());
        verify(jwtTokenManager, never()).generateToken(anyString());
    }

    @Test
    public void testAuthorizeUser_InvalidCredentials_UserNotFound() {
        // Arrange
        String username = "nonExistingUser";
        String password = "password";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            userService.authorizeUser(username, password);
        });

        assertEquals("Invalid credentials", exception.getMessage());
        verify(jwtTokenManager, never()).generateToken(anyString());
    }

    @Test
    public void testUpdateUserImages_Success() {
        // Arrange
        String username = "testuser";
        List<String> imageIds = Arrays.asList("imageId1", "imageId2");

        UserEntity userEntity = new UserEntity();
        userEntity.setId("userId");
        userEntity.setUsername(username);

        ImageEntity image1 = new ImageEntity();
        image1.setId("imageId1");
        ImageEntity image2 = new ImageEntity();
        image2.setId("imageId2");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(userEntity));
        when(imageRepository.findAllByIdIn(imageIds)).thenReturn(Arrays.asList(image1, image2));

        // Act
        userService.updateUserImages(username, imageIds);

        // Assert
        verify(imageRepository, times(1)).saveAll(anyList());
        assertEquals(userEntity, image1.getUser());
        assertEquals(userEntity, image2.getUser());
    }

    @Test
    public void testUpdateUserImages_UserNotFound() {
        // Arrange
        String username = "nonExistingUser";
        List<String> imageIds = Arrays.asList("imageId1", "imageId2");

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userService.updateUserImages(username, imageIds);
        });

        assertEquals("User not found with username: " + username, exception.getMessage());
        verify(imageRepository, never()).saveAll(anyList());
    }

    @Test
    public void testUpdateUserImages_InvalidImageIds() {
        // Arrange
        String username = "testuser";
        List<String> imageIds = Arrays.asList("imageId1", "imageId2");

        UserEntity userEntity = new UserEntity();
        userEntity.setId("userId");
        userEntity.setUsername(username);

        // Only one image is found
        ImageEntity image1 = new ImageEntity();
        image1.setId("imageId1");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(userEntity));
        when(imageRepository.findAllByIdIn(imageIds)).thenReturn(Collections.singletonList(image1));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUserImages(username, imageIds);
        });

        assertEquals("One or more image IDs are invalid.", exception.getMessage());
        verify(imageRepository, never()).saveAll(anyList());
    }
}