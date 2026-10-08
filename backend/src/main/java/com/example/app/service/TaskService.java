package com.example.app.service;

import com.example.app.dto.TaskRequest;
import com.example.app.dto.TaskResponse;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.mapper.TaskMapper;
import com.example.app.model.Task;
import com.example.app.model.User;
import com.example.app.repository.TaskRepository;
import com.example.app.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for tasks. Every method is scoped to the currently logged-in
 * user (identified by email from the JWT) so users only touch their own data.
 */
@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> findAllForUser(String email) {
        User owner = requireUser(email);
        return taskRepository.findByOwnerIdOrderByCreatedAtDesc(owner.getId())
                .stream()
                .map(TaskMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse findOne(Long id, String email) {
        return TaskMapper.toResponse(requireOwnedTask(id, email));
    }

    @Transactional
    public TaskResponse create(TaskRequest request, String email) {
        User owner = requireUser(email);
        Task task = new Task(request.title(), request.description(), owner);
        task.setCompleted(request.completed());
        return TaskMapper.toResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse update(Long id, TaskRequest request, String email) {
        Task task = requireOwnedTask(id, email);
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setCompleted(request.completed());
        return TaskMapper.toResponse(taskRepository.save(task));
    }

    @Transactional
    public void delete(Long id, String email) {
        Task task = requireOwnedTask(id, email);
        taskRepository.delete(task);
    }

    // ----- helpers -----

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));
    }

    /** Loads a task only if it belongs to this user; otherwise 404. */
    private Task requireOwnedTask(Long id, String email) {
        User owner = requireUser(email);
        return taskRepository.findByIdAndOwnerId(id, owner.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));
    }
}
