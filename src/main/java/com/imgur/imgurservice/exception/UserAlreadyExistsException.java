package com.imgur.imgurservice.exception;

/**
 * Custom exception thrown when an attempt is made to register a user that already exists.
 */
public class UserAlreadyExistsException extends RuntimeException {

    /**
     * Constructs a new UserAlreadyExistsException with the specified detail message.
     *
     * @param message the detail message providing context about the exception
     */
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}