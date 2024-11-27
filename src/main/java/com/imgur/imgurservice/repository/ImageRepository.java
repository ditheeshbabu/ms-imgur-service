package com.imgur.imgurservice.repository;

import com.imgur.imgurservice.entity.ImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Repository interface for performing database operations on the Image entity.
 */
@Repository
@Transactional(isolation = Isolation.READ_COMMITTED)
public interface ImageRepository extends JpaRepository<ImageEntity, String> {

    /**
     * Finds all images associated with a specific user by the user's ID.
     *
     * @param userId the ID of the user
     * @return a list of ImageEntity objects associated with the user
     */
    List<ImageEntity> findByUserId(String userId);

    /**
     * Finds all ImageEntity objects with IDs in the specified list.
     *
     * @param ids the list of image IDs to fetch
     * @return a list of ImageEntity objects matching the provided IDs
     */
    List<ImageEntity> findAllByIdIn(List<String> ids);

    // Additional custom query methods can be added here as needed.
}