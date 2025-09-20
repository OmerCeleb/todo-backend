package com.todoapp.service;

import com.todoapp.dto.TodoRequestDTO;
import com.todoapp.dto.TodoResponseDTO;
import com.todoapp.dto.TodoStatsDTO;
import com.todoapp.entity.Todo;
import com.todoapp.repository.TodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service layer for Todo business logic.
 * Handles data transformation, validation, and business rules.
 */
@Service
@Transactional
public class TodoService {

    private final TodoRepository todoRepository;

    /**
     * Constructor with dependency injection
     * @param todoRepository Repository for database operations
     */
    @Autowired
    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    /**
     * Get all todos as response DTOs
     * @return List of all todos
     */
    public List<TodoResponseDTO> getAllTodos() {
        return todoRepository.findAll()
                .stream()
                .map(TodoResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Get todo by ID
     * @param id Todo ID
     * @return Optional TodoResponseDTO
     */
    public Optional<TodoResponseDTO> getTodoById(Long id) {
        return todoRepository.findById(id)
                .map(TodoResponseDTO::new);
    }

    /**
     * Create a new todo
     * @param todoRequest Request DTO with todo data
     * @return Created todo as response DTO
     */
    public TodoResponseDTO createTodo(TodoRequestDTO todoRequest) {
        Todo todo = new Todo();
        mapRequestToEntity(todoRequest, todo);

        Todo savedTodo = todoRepository.save(todo);
        return new TodoResponseDTO(savedTodo);
    }

    /**
     * Update an existing todo
     * @param id Todo ID to update
     * @param todoRequest Request DTO with updated data
     * @return Updated todo as response DTO, or empty if not found
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
     * @param id Todo ID to toggle
     * @return Updated todo as response DTO, or empty if not found
     */
    public Optional<TodoResponseDTO> toggleTodo(Long id) {
        return todoRepository.findById(id)
                .map(todo -> {
                    todo.setCompleted(!todo.isCompleted());
                    Todo updatedTodo = todoRepository.save(todo);
                    return new TodoResponseDTO(updatedTodo);
                });
    }

    /**
     * Delete a todo by ID
     * @param id Todo ID to delete
     * @return true if deleted, false if not found
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
     * @param ids List of todo IDs to delete
     */
    public void bulkDeleteTodos(List<Long> ids) {
        todoRepository.deleteAllById(ids);
    }

    /**
     * Get todos by completion status
     * @param completed true for completed, false for active todos
     * @return List of todos with specified completion status
     */
    public List<TodoResponseDTO> getTodosByCompleted(boolean completed) {
        return todoRepository.findByCompleted(completed)
                .stream()
                .map(TodoResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Get todos by priority level
     * @param priority Priority level to filter by
     * @return List of todos with specified priority
     */
    public List<TodoResponseDTO> getTodosByPriority(Todo.Priority priority) {
        return todoRepository.findByPriority(priority)
                .stream()
                .map(TodoResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Get todos by category
     * @param category Category to filter by
     * @return List of todos in specified category
     */
    public List<TodoResponseDTO> getTodosByCategory(String category) {
        return todoRepository.findByCategoryIgnoreCase(category)
                .stream()
                .map(TodoResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Search todos by title or description
     * @param searchTerm Text to search for
     * @return List of todos containing the search term
     */
    public List<TodoResponseDTO> searchTodos(String searchTerm) {
        return todoRepository.searchByTitleOrDescription(searchTerm)
                .stream()
                .map(TodoResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Get filtered todos based on multiple criteria
     * @param completed Completion status filter (null to ignore)
     * @param priority Priority filter (null to ignore)
     * @param category Category filter (null to ignore)
     * @return List of todos matching all non-null criteria
     */
    public List<TodoResponseDTO> getFilteredTodos(Boolean completed, Todo.Priority priority, String category) {
        return todoRepository.findByFilters(completed, priority, category)
                .stream()
                .map(TodoResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Get overdue todos (past due date and not completed)
     * @return List of overdue todos
     */
    public List<TodoResponseDTO> getOverdueTodos() {
        return todoRepository.findByDueDateBeforeAndCompletedFalse(LocalDateTime.now())
                .stream()
                .map(TodoResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Get todos due today
     * @return List of todos due today
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
     * Get all unique categories
     * @return List of all categories used in todos
     */
    public List<String> getAllCategories() {
        return todoRepository.findAllCategories();
    }

    /**
     * Get todo statistics
     * @return Statistics about todos (total, completed, active, overdue)
     */
    public TodoStatsDTO getTodoStats() {
        long total = todoRepository.count();
        long completed = todoRepository.countByCompleted(true);
        long active = todoRepository.countByCompleted(false);
        long overdue = todoRepository.countOverdueTodos(LocalDateTime.now());

        return new TodoStatsDTO(total, completed, active, overdue);
    }

    /**
     * Get active todos ordered by priority and due date
     * @return List of active todos sorted by importance
     */
    public List<TodoResponseDTO> getActiveTodosOrderedByPriority() {
        return todoRepository.findActiveTodosOrderedByPriorityAndDueDate()
                .stream()
                .map(TodoResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Helper method to map request DTO to entity
     * @param request Source DTO
     * @param todo Target entity
     */
    private void mapRequestToEntity(TodoRequestDTO request, Todo todo) {
        todo.setTitle(request.getTitle());
        todo.setDescription(request.getDescription());
        todo.setPriority(request.getPriority() != null ? request.getPriority() : Todo.Priority.MEDIUM);
        todo.setCategory(request.getCategory());
        todo.setDueDate(request.getDueDate());
    }
}