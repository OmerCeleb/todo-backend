package com.todoapp.controller;

import com.todoapp.dto.TodoRequestDTO;
import com.todoapp.dto.TodoResponseDTO;
import com.todoapp.dto.TodoStatsDTO;
import com.todoapp.entity.Todo;
import com.todoapp.service.TodoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for Todo API endpoints.
 * Handles HTTP requests and delegates business logic to TodoService.
 */
@RestController
@RequestMapping("/api/todos")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class TodoController {

    private final TodoService todoService;

    /**
     * Constructor with dependency injection
     * @param todoService Service layer for business logic
     */
    @Autowired
    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    /**
     * Get all todos with optional filtering
     * @param completed Filter by completion status (optional)
     * @param priority Filter by priority level (optional)
     * @param category Filter by category (optional)
     * @param search Search in title/description (optional)
     * @return List of todos matching criteria
     */
    @GetMapping
    public ResponseEntity<List<TodoResponseDTO>> getAllTodos(
            @RequestParam(required = false) Boolean completed,
            @RequestParam(required = false) Todo.Priority priority,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {

        List<TodoResponseDTO> todos;

        if (search != null && !search.trim().isEmpty()) {
            todos = todoService.searchTodos(search);
        } else if (completed != null || priority != null || category != null) {
            todos = todoService.getFilteredTodos(completed, priority, category);
        } else {
            todos = todoService.getAllTodos();
        }

        return ResponseEntity.ok(todos);
    }

    /**
     * Get todo by ID
     * @param id Todo ID
     * @return Todo if found, 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<TodoResponseDTO> getTodoById(@PathVariable Long id) {
        Optional<TodoResponseDTO> todo = todoService.getTodoById(id);
        return todo.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }



    /**
     * Create a new todo
     * @param todoRequest Request DTO with todo data
     * @return Created todo with 201 status
     */
    @PostMapping
    public ResponseEntity<TodoResponseDTO> createTodo(@Valid @RequestBody TodoRequestDTO todoRequest) {
        TodoResponseDTO createdTodo = todoService.createTodo(todoRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTodo);
    }

    /**
     * Update an existing todo
     * @param id Todo ID to update
     * @param todoRequest Request DTO with updated data
     * @return Updated todo if found, 404 if not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<TodoResponseDTO> updateTodo(
            @PathVariable Long id,
            @Valid @RequestBody TodoRequestDTO todoRequest) {

        Optional<TodoResponseDTO> updatedTodo = todoService.updateTodo(id, todoRequest);
        return updatedTodo.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Toggle todo completion status
     * @param id Todo ID to toggle
     * @return Updated todo if found, 404 if not found
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<TodoResponseDTO> toggleTodo(@PathVariable Long id) {
        Optional<TodoResponseDTO> toggledTodo = todoService.toggleTodo(id);
        return toggledTodo.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete a todo
     * @param id Todo ID to delete
     * @return 204 if deleted, 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodo(@PathVariable Long id) {
        boolean deleted = todoService.deleteTodo(id);
        return deleted ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    /**
     * Bulk delete multiple todos
     * @param ids List of todo IDs to delete
     * @return 204 No Content
     */
    @DeleteMapping("/bulk")
    public ResponseEntity<Void> bulkDeleteTodos(@RequestBody List<Long> ids) {
        todoService.bulkDeleteTodos(ids);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get todo statistics
     * @return Statistics about todos (total, completed, active, overdue)
     */
    @GetMapping("/stats")
    public ResponseEntity<TodoStatsDTO> getTodoStats() {
        TodoStatsDTO stats = todoService.getTodoStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get all unique categories
     * @return List of all categories used in todos
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        List<String> categories = todoService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * Get overdue todos
     * @return List of todos that are past their due date and not completed
     */
    @GetMapping("/overdue")
    public ResponseEntity<List<TodoResponseDTO>> getOverdueTodos() {
        List<TodoResponseDTO> overdueTodos = todoService.getOverdueTodos();
        return ResponseEntity.ok(overdueTodos);
    }

    /**
     * Get todos due today
     * @return List of todos due today
     */
    @GetMapping("/due-today")
    public ResponseEntity<List<TodoResponseDTO>> getTodosDueToday() {
        List<TodoResponseDTO> todosDueToday = todoService.getTodosDueToday();
        return ResponseEntity.ok(todosDueToday);
    }

    /**
     * Get active todos sorted by priority and due date
     * @return List of incomplete todos ordered by importance
     */
    @GetMapping("/priority-sorted")
    public ResponseEntity<List<TodoResponseDTO>> getActiveTodosOrderedByPriority() {
        List<TodoResponseDTO> sortedTodos = todoService.getActiveTodosOrderedByPriority();
        return ResponseEntity.ok(sortedTodos);
    }

    /**
     * Get completed todos
     * @return List of completed todos
     */
    @GetMapping("/completed")
    public ResponseEntity<List<TodoResponseDTO>> getCompletedTodos() {
        List<TodoResponseDTO> completedTodos = todoService.getTodosByCompleted(true);
        return ResponseEntity.ok(completedTodos);
    }

    /**
     * Get active (incomplete) todos
     * @return List of active todos
     */
    @GetMapping("/active")
    public ResponseEntity<List<TodoResponseDTO>> getActiveTodos() {
        List<TodoResponseDTO> activeTodos = todoService.getTodosByCompleted(false);
        return ResponseEntity.ok(activeTodos);
    }
}