package com.example.app.controller;

import com.example.app.dto.TaskRequest;
import com.example.app.dto.TaskResponse;
import com.example.app.routes.ApiRoutes;
import com.example.app.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Protected REST endpoints for tasks. These require a valid JWT.
 *
 *   GET    /api/tasks        -> list my tasks
 *   GET    /api/tasks/{id}   -> get one of my tasks
 *   POST   /api/tasks        -> create a task
 *   PUT    /api/tasks/{id}   -> update a task
 *   DELETE /api/tasks/{id}   -> delete a task
 *
 * {@code @AuthenticationPrincipal} gives us the logged-in user that the
 * JwtAuthenticationFilter put into the security context; getUsername() is
 * the email.
 */
@RestController
@RequestMapping(ApiRoutes.TASKS)
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<TaskResponse> list(@AuthenticationPrincipal UserDetails user) {
        return taskService.findAllForUser(user.getUsername());
    }

    @GetMapping(ApiRoutes.TASK_BY_ID)
    public TaskResponse getOne(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        return taskService.findOne(id, user.getUsername());
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest request,
                                               @AuthenticationPrincipal UserDetails user) {
        TaskResponse created = taskService.create(request, user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping(ApiRoutes.TASK_BY_ID)
    public TaskResponse update(@PathVariable Long id,
                               @Valid @RequestBody TaskRequest request,
                               @AuthenticationPrincipal UserDetails user) {
        return taskService.update(id, request, user.getUsername());
    }

    @DeleteMapping(ApiRoutes.TASK_BY_ID)
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        taskService.delete(id, user.getUsername());
        return ResponseEntity.noContent().build();
    }
}
