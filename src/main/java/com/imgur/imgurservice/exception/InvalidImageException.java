package com.imgur.imgurservice.exception;

/**
 * Custom exception thrown when access to a resource is denied.
 */
public class InvalidImageException extends RuntimeException {

    /**
     * Constructs a new InvalidImageException with the specified detail message.
     *
     * @param message the detail message providing context about the exception
     */
    public InvalidImageException(String message) {
        super(message);
    }
}