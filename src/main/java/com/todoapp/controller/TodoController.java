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

    // ==================== EXISTING ENDPOINTS ====================

    @GetMapping
    public ResponseEntity<?> getAllUserTodos(
            @RequestParam(required = false) Boolean completed,
            @RequestParam(required = false) Todo.Priority priority,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {

        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "User not authenticated"));
            }

            List<TodoResponseDTO> todos = todoService.getUserTodos(
                    currentUser.getId(), completed, priority, category, search
            );

            return ResponseEntity.ok(todos);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to fetch todos: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTodoById(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "User not authenticated"));
            }

            Optional<TodoResponseDTO> todo = todoService.getUserTodoById(currentUser.getId(), id);

            if (todo.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Todo not found or access denied"));
            }

            return ResponseEntity.ok(todo.get());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to fetch todo: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createTodo(@Valid @RequestBody TodoRequestDTO todoRequest) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "User not authenticated"));
            }

            TodoResponseDTO createdTodo = todoService.createTodoForUser(
                    currentUser.getId(), todoRequest
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(createdTodo);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to create todo: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTodo(
            @PathVariable Long id,
            @Valid @RequestBody TodoRequestDTO todoRequest) {

        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "User not authenticated"));
            }

            Optional<TodoResponseDTO> updatedTodo = todoService.updateUserTodo(
                    currentUser.getId(), id, todoRequest
            );

            if (updatedTodo.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Todo not found or access denied"));
            }

            return ResponseEntity.ok(updatedTodo.get());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to update todo: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTodo(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "User not authenticated"));
            }

            boolean deleted = todoService.deleteUserTodo(currentUser.getId(), id);

            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Todo not found or access denied"));
            }

            return ResponseEntity.ok(Map.of("message", "Todo deleted successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to delete todo: " + e.getMessage()));
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> toggleTodoCompletion(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "User not authenticated"));
            }

            Optional<TodoResponseDTO> updatedTodo = todoService.toggleUserTodoCompletion(
                    currentUser.getId(), id
            );

            if (updatedTodo.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Todo not found or access denied"));
            }

            return ResponseEntity.ok(updatedTodo.get());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to toggle todo: " + e.getMessage()));
        }
    }

    // ==================== NEW ENDPOINTS ====================

    /**
     * Bulk delete multiple todos by IDs
     * Frontend expects: POST /api/todos/bulk-delete with body: { ids: [1, 2, 3] }
     */
    @PostMapping("/bulk-delete")
    public ResponseEntity<?> bulkDeleteTodos(@RequestBody Map<String, List<Long>> request) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "User not authenticated"));
            }

            List<Long> ids = request.get("ids");
            if (ids == null || ids.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "No todo IDs provided"));
            }

            int deletedCount = todoService.bulkDeleteUserTodos(currentUser.getId(), ids);

            return ResponseEntity.ok(Map.of(
                    "message", "Todos deleted successfully",
                    "deletedCount", deletedCount
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to delete todos: " + e.getMessage()));
        }
    }

    /**
     * Reorder todos (for drag-and-drop)
     * Frontend expects: POST /api/todos/reorder with body: [{ id: 1, order: 0 }, { id: 2, order: 1 }]
     */
    @PostMapping("/reorder")
    public ResponseEntity<?> reorderTodos(@RequestBody List<Map<String, Object>> reorderData) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "User not authenticated"));
            }

            if (reorderData == null || reorderData.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "No reorder data provided"));
            }

            todoService.reorderUserTodos(currentUser.getId(), reorderData);

            return ResponseEntity.ok(Map.of("message", "Todos reordered successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to reorder todos: " + e.getMessage()));
        }
    }

    /**
     * Get all unique categories for the user's todos
     * Frontend expects: GET /api/todos/categories returning string[]
     */
    @GetMapping("/categories")
    public ResponseEntity<?> getUserCategories() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "User not authenticated"));
            }

            List<String> categories = todoService.getUserCategories(currentUser.getId());
            return ResponseEntity.ok(categories);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to fetch categories: " + e.getMessage()));
        }
    }

    /**
     * Get statistics for the user's todos
     * Frontend expects: { total, completed, active, overdue }
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getUserTodoStats() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "User not authenticated"));
            }

            TodoStatsDTO stats = todoService.getUserTodoStats(currentUser.getId());
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to fetch stats: " + e.getMessage()));
        }
    }

    /**
     * Get overdue todos for the user
     */
    @GetMapping("/overdue")
    public ResponseEntity<?> getOverdueTodos() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "User not authenticated"));
            }

            List<TodoResponseDTO> overdueTodos = todoService.getUserOverdueTodos(currentUser.getId());
            return ResponseEntity.ok(overdueTodos);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to fetch overdue todos: " + e.getMessage()));
        }
    }

    /**
     * Delete all completed todos for the user
     */
    @DeleteMapping("/completed")
    public ResponseEntity<?> deleteCompletedTodos() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "User not authenticated"));
            }

            int deletedCount = todoService.deleteUserCompletedTodos(currentUser.getId());

            return ResponseEntity.ok(Map.of(
                    "message", "Completed todos deleted successfully",
                    "deletedCount", deletedCount
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to delete completed todos: " + e.getMessage()));
        }
    }
}