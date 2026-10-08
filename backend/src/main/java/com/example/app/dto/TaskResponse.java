package com.example.app.dto;

import java.time.Instant;

/**
 * Outgoing representation of a task. We expose a DTO (not the entity) so the
 * API shape is decoupled from the database model and we never leak the owner's
 * password or other internal fields.
 */
public record TaskResponse(
        Long id,
        String title,
        String description,
        boolean completed,
        Instant createdAt,
        Instant updatedAt
) {
}
