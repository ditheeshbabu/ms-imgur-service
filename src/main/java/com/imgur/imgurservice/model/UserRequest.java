package com.imgur.imgurservice.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserRequest {
    @NotBlank
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;
    @NotBlank
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;
    @NotBlank
    @Email(message = "Email should be valid")
    private String email;
}