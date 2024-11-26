package com.imgur.imgurservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Entity class representing the User table in the database.
 * This class holds user details like username, password, and email.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    /**
     * Primary key for the User entity.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /**
     * Username of the user. Must be unique and not null.
     */
    @Column(name = "USERNAME", unique = true, nullable = false)
    @NotNull(message = "Username cannot be null")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;

    /**
     * Password for the user. Must be encrypted and not null.
     */
    @Column(name = "PASSWORD", nullable = false)
    @NotNull(message = "Password cannot be null")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    /**
     * Email address of the user. Must follow a valid email format.
     */
    @Column(name = "EMAIL")
    @Email(message = "Email should be valid")
    private String email;

    /**
     * List of images associated with the user.
     * Establishes a one-to-many relationship with the ImageEntity.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImageEntity> images;
}