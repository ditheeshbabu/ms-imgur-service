package com.imgur.imgurservice;

import com.imgur.imgurservice.repository.ImageRepository;
import com.imgur.imgurservice.repository.UserRepository;
import com.imgur.imgurservice.util.JwtTokenManager;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ImgurserviceApplicationTests {

	@Mock
	private UserRepository userRepository;

	@Mock
	private ImageRepository imageRepository;

	@Mock
	private JwtTokenManager jwtTokenManager;

	@Mock
	private RestTemplate restTemplate;

	@Test
	void contextLoads() {
		// Verifies that the application context loads successfully
		assertNotNull(userRepository);
		assertNotNull(imageRepository);
		assertNotNull(jwtTokenManager);
		assertNotNull(restTemplate);
	}
}