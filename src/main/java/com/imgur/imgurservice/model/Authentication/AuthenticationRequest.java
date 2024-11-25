package com.imgur.imgurservice.model.Authentication;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a request containing credentials for authentication.
 * This class is typically used for login requests where a user provides
 * their username and password to gain access to the system.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationRequest {

    /**
     * The username of the user attempting to authenticate.
     * This field is required and should be a valid identifier for the user.
     */
    private String username;

    /**
     * The password of the user attempting to authenticate.
     * This field is required and should match the user's stored credentials.
     */
    private String password;
}