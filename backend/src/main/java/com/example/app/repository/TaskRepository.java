package com.example.app.repository;

import com.example.app.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Queries are scoped by owner so users can only ever see their own tasks.
 * Spring Data derives the SQL from these method names automatically.
 */
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    Optional<Task> findByIdAndOwnerId(Long id, Long ownerId);
}
