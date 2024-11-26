package com.imgur.imgurservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity class representing the Image table in the database.
 * This class stores details of images uploaded by users.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageEntity {

    /**
     * Primary key for the Image entity.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /**
     * URL of the uploaded image on Imgur.
     * This field is mandatory.
     */
    @Column(nullable = false)
    @NotNull(message = "Image URL cannot be null")
    private String imageUrl;

    /**
     * Delete hash provided by Imgur for managing image deletions.
     * This field is mandatory.
     */
    @Column(nullable = false)
    @NotNull(message = "Delete hash cannot be null")
    private String deleteHash;

    /**
     * The user who owns this image.
     * Establishes a many-to-one relationship with the User entity.
     */

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User association cannot be null")
    private UserEntity user;
}