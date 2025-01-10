package com.imgur.imgurservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Response object for image details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageResponse implements Serializable {

    /**
     * The unique ID of the image.
     */
    private String imageId;

    /**
     * The URL of the image.
     */
    private String imageUrl;

}