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
import java.util.Map;
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

    // ==================== EXISTING METHODS ====================

    /**
     * Get all todos for a specific user with optional filtering
     */
    public List<TodoResponseDTO> getUserTodos(Long userId, Boolean completed, Todo.Priority priority,
                                              String category, String search) {
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
        long completedCount = userTodos.stream().filter(Todo::getCompleted).count();
        long activeCount = totalCount - completedCount;
        long overdueCount = userTodos.stream()
                .filter(todo -> !todo.getCompleted() && todo.isOverdue())
                .count();

        return new TodoStatsDTO(totalCount, completedCount, activeCount, overdueCount);
    }

    /**
     * Get overdue todos for a user
     */
    public List<TodoResponseDTO> getUserOverdueTodos(Long userId) {
        List<Todo> overdueTodos = todoRepository.findByUserId(userId).stream()
                .filter(todo -> !todo.getCompleted() && todo.isOverdue())
                .collect(Collectors.toList());

        return overdueTodos.stream()
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

    // ==================== NEW METHODS ====================

    /**
     * Bulk delete multiple todos for a user (security check)
     * @param userId User ID
     * @param todoIds List of todo IDs to delete
     * @return Number of deleted todos
     */
    public int bulkDeleteUserTodos(Long userId, List<Long> todoIds) {
        if (todoIds == null || todoIds.isEmpty()) {
            return 0;
        }

        // Security check: only delete todos that belong to this user
        List<Todo> todosToDelete = todoRepository.findAllById(todoIds).stream()
                .filter(todo -> todo.getUser().getId().equals(userId))
                .collect(Collectors.toList());

        int deletedCount = todosToDelete.size();
        todoRepository.deleteAll(todosToDelete);

        return deletedCount;
    }

    /**
     * Reorder todos for drag-and-drop functionality
     * @param userId User ID
     * @param reorderData List of maps containing id and order
     */
    public void reorderUserTodos(Long userId, List<Map<String, Object>> reorderData) {
        for (Map<String, Object> item : reorderData) {
            Object idObj = item.get("id");
            Object orderObj = item.get("order");

            if (idObj == null || orderObj == null) {
                continue;
            }

            // Convert to Long (handle both Integer and Long)
            Long todoId = idObj instanceof Integer ? ((Integer) idObj).longValue() : (Long) idObj;
            Integer order = orderObj instanceof Integer ? (Integer) orderObj :
                    orderObj instanceof Double ? ((Double) orderObj).intValue() : null;

            if (order == null) {
                continue;
            }

            // Security check: only update todos that belong to this user
            Optional<Todo> todoOpt = todoRepository.findByIdAndUserId(todoId, userId);
            if (todoOpt.isPresent()) {
                Todo todo = todoOpt.get();
                todo.setDisplayOrder(order);
                todoRepository.save(todo);
            }
        }
    }

    /**
     * Get all unique categories for a user's todos
     * @param userId User ID
     * @return List of unique category names
     */
    public List<String> getUserCategories(Long userId) {
        List<Todo> userTodos = todoRepository.findByUserId(userId);

        return userTodos.stream()
                .map(Todo::getCategory)
                .filter(category -> category != null && !category.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    // ==================== HELPER METHODS ====================

    /**
     * Map TodoRequestDTO to Todo entity
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