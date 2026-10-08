package com.example.app.mapper;

import com.example.app.dto.TaskResponse;
import com.example.app.model.Task;

/**
 * Converts between the {@link Task} entity and its DTOs.
 * Keeping this in one place makes the mapping easy to find and change.
 * (For bigger projects, libraries like MapStruct can generate this for you.)
 */
public final class TaskMapper {

    private TaskMapper() {
    }

    public static TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.isCompleted(),
                task.getCreatedAt(),
                task.getUpdatedAt());
    }
}
