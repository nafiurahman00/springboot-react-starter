package com.example.app.exception;

/**
 * Thrown during registration when the email is already taken.
 * Mapped to HTTP 409 (Conflict) by {@link GlobalExceptionHandler}.
 */
public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super("Email already registered: " + email);
    }
}
