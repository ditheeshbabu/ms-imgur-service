package com.imgur.imgurservice.exception;

/**
 * Custom exception thrown when a requested username is not found in the system.
 */
public class UsernameNotFoundException extends RuntimeException {

    /**
     * Constructs a new UsernameNotFoundException with the specified detail message.
     *
     * @param message the detail message providing context about the exception
     */
    public UsernameNotFoundException(String message) {
        super(message);
    }
}