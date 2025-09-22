// src/main/java/com/todoapp/controller/TodoController.java
package com.todoapp.controller;

import com.todoapp.dto.TodoRequestDTO;
import com.todoapp.dto.TodoResponseDTO;
import com.todoapp.dto.TodoStatsDTO;
import com.todoapp.entity.Todo;
import com.todoapp.entity.User;
import com.todoapp.service.TodoService;
import com.todoapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for Todo API endpoints with user authentication.
 * Handles HTTP requests and delegates business logic to TodoService.
 * All operations are now user-specific based on JWT authentication.
 */
@RestController
@RequestMapping("/api/todos")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class TodoController {

    private final TodoService todoService;
    private final UserService userService;

    @Autowired
    public TodoController(TodoService todoService, UserService userService) {
        this.todoService = todoService;
        this.userService = userService;
    }

    /**
     * Get current authenticated user
     * @return Current user or null if not authenticated
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String email = authentication.getName();
        Optional<User> userOpt = userService.findActiveUserByEmail(email);
        return userOpt.orElse(null);
    }

    /**
     * Get all todos for the authenticated user with optional filtering
     * @param completed Filter by completion status (optional)
     * @param priority Filter by priority level (optional)
     * @param category Filter by category (optional)
     * @param search Search in title/description (optional)
     * @return List of user's todos matching criteria
     */
    @GetMapping
    public ResponseEntity<?> getAllUserTodos(
            @RequestParam(required = false) Boolean completed,
            @RequestParam(required = false) Todo.Priority priority,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {

        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            List<TodoResponseDTO> todos = todoService.getUserTodos(
                    currentUser.getId(), completed, priority, category, search
            );

            return ResponseEntity.ok(todos);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch todos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get a specific todo by ID (must belong to authenticated user)
     * @param id Todo ID
     * @return Todo details
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getTodoById(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            Optional<TodoResponseDTO> todo = todoService.getUserTodoById(currentUser.getId(), id);
            if (todo.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Todo not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            return ResponseEntity.ok(todo.get());

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch todo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Create a new todo for the authenticated user
     * @param todoRequest Todo data
     * @return Created todo
     */
    @PostMapping
    public ResponseEntity<?> createTodo(@Valid @RequestBody TodoRequestDTO todoRequest) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            TodoResponseDTO createdTodo = todoService.createTodoForUser(currentUser.getId(), todoRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTodo);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to create todo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Update an existing todo (must belong to authenticated user)
     * @param id Todo ID
     * @param todoRequest Updated todo data
     * @return Updated todo
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTodo(@PathVariable Long id,
                                        @Valid @RequestBody TodoRequestDTO todoRequest) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            Optional<TodoResponseDTO> updatedTodo = todoService.updateUserTodo(
                    currentUser.getId(), id, todoRequest
            );

            if (updatedTodo.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Todo not found or access denied");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            return ResponseEntity.ok(updatedTodo.get());

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to update todo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Delete a todo (must belong to authenticated user)
     * @param id Todo ID
     * @return Success message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTodo(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            boolean deleted = todoService.deleteUserTodo(currentUser.getId(), id);
            if (!deleted) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Todo not found or access denied");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            Map<String, String> response = new HashMap<>();
            response.put("message", "Todo deleted successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to delete todo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Toggle todo completion status
     * @param id Todo ID
     * @return Updated todo
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<?> toggleTodoCompletion(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            Optional<TodoResponseDTO> updatedTodo = todoService.toggleUserTodoCompletion(
                    currentUser.getId(), id
            );

            if (updatedTodo.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Todo not found or access denied");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            return ResponseEntity.ok(updatedTodo.get());

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to toggle todo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get todo statistics for the authenticated user
     * @return Todo statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getUserTodoStats() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            TodoStatsDTO stats = todoService.getUserTodoStats(currentUser.getId());
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch stats: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get user's overdue todos
     * @return List of overdue todos
     */
    @GetMapping("/overdue")
    public ResponseEntity<?> getOverdueTodos() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            List<TodoResponseDTO> overdueTodos = todoService.getUserOverdueTodos(currentUser.getId());
            return ResponseEntity.ok(overdueTodos);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch overdue todos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Bulk delete completed todos
     * @return Number of deleted todos
     */
    @DeleteMapping("/completed")
    public ResponseEntity<?> deleteCompletedTodos() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            int deletedCount = todoService.deleteUserCompletedTodos(currentUser.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Completed todos deleted successfully");
            response.put("deletedCount", deletedCount);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to delete completed todos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}