package com.imgur.imgurservice.model.Authentication;

import lombok.*;

/**
 * Represents a response containing a JWT (JSON Web Token) for authentication.
 * This class is typically used to send the generated JWT token back to the client
 * after successful authentication or authorization.
 */
@Getter
@Setter
@AllArgsConstructor
@Builder
public class JwtResponse {

    /**
     * The JSON Web Token (JWT) issued to the authenticated user.
     * This token is used to authorize subsequent requests and should be included
     * in the `Authorization` header of API calls.
     */
    private final String jwt;
}