// src/main/java/com/todoapp/service/TodoService.java
package com.todoapp.service;

import com.todoapp.dto.TodoRequestDTO;
import com.todoapp.dto.TodoResponseDTO;
import com.todoapp.dto.TodoStatsDTO;
import com.todoapp.entity.Todo;
import com.todoapp.entity.User;
import com.todoapp.repository.TodoRepository;
import com.todoapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service layer for Todo business logic with user authentication support.
 * Handles data transformation, validation, and business rules.
 */
@Service
@Transactional
public class TodoService {

    private final TodoRepository todoRepository;
    private final UserRepository userRepository;

    @Autowired
    public TodoService(TodoRepository todoRepository, UserRepository userRepository) {
        this.todoRepository = todoRepository;
        this.userRepository = userRepository;
    }

    // ================================================
    // USER-SPECIFIC METHODS (NEW)
    // ================================================

    /**
     * Get all todos for a specific user with optional filtering
     */
    public List<TodoResponseDTO> getUserTodos(Long userId, Boolean completed, Todo.Priority priority, String category, String search) {
        List<Todo> todos;

        if (search != null && !search.trim().isEmpty()) {
            todos = todoRepository.findByUserIdAndTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                    userId, search, search);
        } else if (completed != null || priority != null || category != null) {
            todos = todoRepository.findByUserIdWithFilters(userId, completed, priority, category);
        } else {
            todos = todoRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }

        return todos.stream()
                .map(TodoResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific todo by ID for a user (security check)
     */
    public Optional<TodoResponseDTO> getUserTodoById(Long userId, Long todoId) {
        return todoRepository.findByIdAndUserId(todoId, userId)
                .map(TodoResponseDTO::new);
    }

    /**
     * Create a new todo for a user
     */
    public TodoResponseDTO createTodoForUser(Long userId, TodoRequestDTO todoRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Todo todo = new Todo();
        mapRequestToEntity(todoRequest, todo);
        todo.setUser(user);

        Todo savedTodo = todoRepository.save(todo);
        return new TodoResponseDTO(savedTodo);
    }

    /**
     * Update a user's todo (security check)
     */
    public Optional<TodoResponseDTO> updateUserTodo(Long userId, Long todoId, TodoRequestDTO todoRequest) {
        return todoRepository.findByIdAndUserId(todoId, userId)
                .map(todo -> {
                    mapRequestToEntity(todoRequest, todo);
                    Todo updatedTodo = todoRepository.save(todo);
                    return new TodoResponseDTO(updatedTodo);
                });
    }

    /**
     * Delete a user's todo (security check)
     */
    public boolean deleteUserTodo(Long userId, Long todoId) {
        Optional<Todo> todoOpt = todoRepository.findByIdAndUserId(todoId, userId);
        if (todoOpt.isPresent()) {
            todoRepository.delete(todoOpt.get());
            return true;
        }
        return false;
    }

    /**
     * Toggle completion status for a user's todo
     */
    public Optional<TodoResponseDTO> toggleUserTodoCompletion(Long userId, Long todoId) {
        return todoRepository.findByIdAndUserId(todoId, userId)
                .map(todo -> {
                    todo.toggleCompleted();
                    Todo updatedTodo = todoRepository.save(todo);
                    return new TodoResponseDTO(updatedTodo);
                });
    }

    /**
     * Get todo statistics for a user
     */
    public TodoStatsDTO getUserTodoStats(Long userId) {
        List<Todo> userTodos = todoRepository.findByUserId(userId);

        long totalCount = userTodos.size();
        long completedCount = userTodos.stream().mapToLong(todo -> todo.getCompleted() ? 1 : 0).sum();
        long activeCount = totalCount - completedCount;
        long overdueCount = userTodos.stream()
                .mapToLong(todo -> todo.isOverdue() ? 1 : 0)
                .sum();

        return new TodoStatsDTO(totalCount, completedCount, activeCount, overdueCount);
    }

    /**
     * Get overdue todos for a user
     */
    public List<TodoResponseDTO> getUserOverdueTodos(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        return todoRepository.findByUserIdAndDueDateBeforeAndCompletedFalse(userId, now)
                .stream()
                .map(TodoResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Delete all completed todos for a user
     */
    public int deleteUserCompletedTodos(Long userId) {
        List<Todo> completedTodos = todoRepository.findByUserIdAndCompleted(userId, true);
        int count = completedTodos.size();
        todoRepository.deleteAll(completedTodos);
        return count;
    }

    // ================================================
    // ORIGINAL METHODS (KEPT FOR BACKWARD COMPATIBILITY)
    // ================================================

    /**
     * Get all todos as response DTOs
     */
    public List<TodoResponseDTO> getAllTodos() {
        return todoRepository.findAll()
                .stream()
                .map(TodoResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Get todo by ID
     */
    public Optional<TodoResponseDTO> getTodoById(Long id) {
        return todoRepository.findById(id)
                .map(TodoResponseDTO::new);
    }

    /**
     * Create a new todo (without user - deprecated)
     */
    @Deprecated
    public TodoResponseDTO createTodo(TodoRequestDTO todoRequest) {
        Todo todo = new Todo();
        mapRequestToEntity(todoRequest, todo);
        // Note: This will fail because user is required now
        Todo savedTodo = todoRepository.save(todo);
        return new TodoResponseDTO(savedTodo);
    }

    /**
     * Update an existing todo
     */
    public Optional<TodoResponseDTO> updateTodo(Long id, TodoRequestDTO todoRequest) {
        return todoRepository.findById(id)
                .map(todo -> {
                    mapRequestToEntity(todoRequest, todo);
                    Todo updatedTodo = todoRepository.save(todo);
                    return new TodoResponseDTO(updatedTodo);
                });
    }

    /**
     * Toggle todo completion status
     */
    public Optional<TodoResponseDTO> toggleTodo(Long id) {
        return todoRepository.findById(id)
                .map(todo -> {
                    todo.toggleCompleted();
                    Todo updatedTodo = todoRepository.save(todo);
                    return new TodoResponseDTO(updatedTodo);
                });
    }

    /**
     * Delete a todo by ID
     */
    public boolean deleteTodo(Long id) {
        if (todoRepository.existsById(id)) {
            todoRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Bulk delete multiple todos
     */
    public void bulkDeleteTodos(List<Long> ids) {
        todoRepository.deleteAllById(ids);
    }

    /**
     * Get todos by completion status
     */
    public List<TodoResponseDTO> getTodosByCompleted(boolean completed) {
        return todoRepository.findByCompleted(completed)
                .stream()
                .map(TodoResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Get todos by priority level
     */
    public List<TodoResponseDTO> getTodosByPriority(Todo.Priority priority) {
        return todoRepository.findByPriority(priority)
                .stream()
                .map(TodoResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Get todos by category
     */
    public List<TodoResponseDTO> getTodosByCategory(String category) {
        return todoRepository.findByCategoryIgnoreCase(category)
                .stream()
                .map(TodoResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Search todos by title or description
     */
    public List<TodoResponseDTO> searchTodos(String searchTerm) {
        return todoRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(searchTerm, searchTerm)
                .stream()
                .map(TodoResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Get filtered todos
     */
    public List<TodoResponseDTO> getFilteredTodos(Boolean completed, Todo.Priority priority, String category) {
        List<Todo> todos = todoRepository.findAll();

        return todos.stream()
                .filter(todo -> completed == null || todo.getCompleted().equals(completed))
                .filter(todo -> priority == null || todo.getPriority().equals(priority))
                .filter(todo -> category == null || category.isEmpty() ||
                        (todo.getCategory() != null && todo.getCategory().equalsIgnoreCase(category)))
                .map(TodoResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Get todo statistics
     */
    public TodoStatsDTO getTodoStats() {
        List<Todo> allTodos = todoRepository.findAll();

        long totalCount = allTodos.size();
        long completedCount = allTodos.stream().mapToLong(todo -> todo.getCompleted() ? 1 : 0).sum();
        long activeCount = totalCount - completedCount;
        long overdueCount = allTodos.stream()
                .mapToLong(todo -> todo.isOverdue() ? 1 : 0)
                .sum();

        return new TodoStatsDTO(totalCount, completedCount, activeCount, overdueCount);
    }

    /**
     * Get all unique categories
     */
    public List<String> getAllCategories() {
        return todoRepository.findDistinctCategories();
    }

    /**
     * Get overdue todos
     */
    public List<TodoResponseDTO> getOverdueTodos() {
        LocalDateTime now = LocalDateTime.now();
        return todoRepository.findByDueDateBeforeAndCompletedFalse(now)
                .stream()
                .map(TodoResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Get todos due today
     */
    public List<TodoResponseDTO> getTodosDueToday() {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);

        return todoRepository.findByDueDateBetween(startOfDay, endOfDay)
                .stream()
                .map(TodoResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Get active todos ordered by priority
     */
    public List<TodoResponseDTO> getActiveTodosOrderedByPriority() {
        return todoRepository.findByCompletedFalseOrderByPriorityDescDueDateAsc()
                .stream()
                .map(TodoResponseDTO::new)
                .collect(Collectors.toList());
    }

    // ================================================
    // UTILITY METHODS
    // ================================================

    /**
     * Map request DTO to entity
     */
    private void mapRequestToEntity(TodoRequestDTO request, Todo todo) {
        todo.setTitle(request.getTitle());
        todo.setDescription(request.getDescription());
        todo.setPriority(request.getPriority());
        todo.setCategory(request.getCategory());
        todo.setDueDate(request.getDueDate());

        if (request.getCompleted() != null) {
            todo.setCompleted(request.getCompleted());
        }
    }
}