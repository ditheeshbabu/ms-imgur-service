package com.imgur.imgurservice.exception;

/**
 * Custom exception thrown when access to a resource is denied.
 */
public class AccessDeniedException extends RuntimeException {

    /**
     * Constructs a new AccessDeniedException with the specified detail message.
     *
     * @param message the detail message providing context about the exception
     */
    public AccessDeniedException(String message) {
        super(message);
    }
}