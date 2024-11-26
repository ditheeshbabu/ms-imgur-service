package com.imgur.imgurservice.exception;

/**
 * Custom exception thrown when an image resource is not found.
 */
public class ImageNotFoundException extends RuntimeException {

    /**
     * Constructs a new ImageNotFoundException with the specified detail message.
     *
     * @param message the detail message providing context about the exception
     */
    public ImageNotFoundException(String message) {
        super(message);
    }
}