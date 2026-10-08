package com.example.app.dto;

/**
 * Returned by register/login. The client stores {@code token} and sends it
 * back as "Authorization: Bearer &lt;token&gt;" on future requests.
 */
public record AuthResponse(
        String token,
        Long userId,
        String name,
        String email,
        String role
) {
}
