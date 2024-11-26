package com.imgur.imgurservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class for application-level settings and beans.
 * Provides a bean for password encoding.
 */
@Configuration
public class AppConfig {

    /**
     * Configures a PasswordEncoder bean using BCrypt for secure password hashing.
     *
     * @return an instance of BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    // Additional beans can be added here as needed.
}