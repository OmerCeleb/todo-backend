// src/main/java/com/todoapp/repository/TodoRepository.java
package com.todoapp.repository;

import com.todoapp.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Todo entities.
 * Extends JpaRepository to provide CRUD operations and custom query methods.
 *
 * Spring Boot automatically provides the following methods:
 * - save(todo) - Save or update a todo
 * - findById(id) - Find todo by ID
 * - findAll() - Get all todos
 * - deleteById(id) - Delete todo by ID
 * - count() - Count total todos
 */
@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {

    /**
     * Find todos by completion status
     * @param completed true for completed todos, false for active todos
     * @return List of todos matching the completion status
     */
    List<Todo> findByCompleted(boolean completed);

    /**
     * Find todos by priority level
     * @param priority Priority enum (LOW, MEDIUM, HIGH)
     * @return List of todos with specified priority
     */
    List<Todo> findByPriority(Todo.Priority priority);

    /**
     * Find todos by exact category match
     * @param category Category name
     * @return List of todos in the specified category
     */
    List<Todo> findByCategory(String category);

    /**
     * Find todos by category (case-insensitive)
     * @param category Category name
     * @return List of todos matching category regardless of case
     */
    List<Todo> findByCategoryIgnoreCase(String category);

    /**
     * Find overdue todos (due date has passed and not completed)
     * @param date Current date/time
     * @return List of overdue incomplete todos
     */
    List<Todo> findByDueDateBeforeAndCompletedFalse(LocalDateTime date);

    /**
     * Find todos with due dates within a specific time range
     * @param start Start of date range
     * @param end End of date range
     * @return List of todos due within the specified range
     */
    List<Todo> findByDueDateBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Search todos by title or description content
     * @param searchTerm Text to search for
     * @return List of todos containing the search term
     */
    @Query("SELECT t FROM Todo t WHERE " +
            "LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Todo> searchByTitleOrDescription(@Param("searchTerm") String searchTerm);

    /**
     * Find todos by multiple filter criteria
     * @param completed Completion status (null to ignore)
     * @param priority Priority level (null to ignore)
     * @param category Category name (null to ignore)
     * @return List of todos matching all non-null criteria
     */
    @Query("SELECT t FROM Todo t WHERE " +
            "(:completed IS NULL OR t.completed = :completed) AND " +
            "(:priority IS NULL OR t.priority = :priority) AND " +
            "(:category IS NULL OR LOWER(t.category) = LOWER(:category))")
    List<Todo> findByFilters(@Param("completed") Boolean completed,
                             @Param("priority") Todo.Priority priority,
                             @Param("category") String category);

    /**
     * Get all unique categories from existing todos
     * @return List of distinct category names, sorted alphabetically
     */
    @Query("SELECT DISTINCT t.category FROM Todo t WHERE t.category IS NOT NULL ORDER BY t.category")
    List<String> findAllCategories();

    /**
     * Count todos by completion status
     * @param completed true for completed, false for active
     * @return Number of todos with specified completion status
     */
    long countByCompleted(boolean completed);

    /**
     * Count overdue todos (past due date and not completed)
     * @param now Current date/time
     * @return Number of overdue incomplete todos
     */
    @Query("SELECT COUNT(t) FROM Todo t WHERE t.dueDate < :now AND t.completed = false")
    long countOverdueTodos(@Param("now") LocalDateTime now);

    /**
     * Find recent todos created after a specific date
     * @param since Date threshold for "recent" todos
     * @return List of todos created after the specified date, ordered by creation time
     */
    @Query("SELECT t FROM Todo t WHERE t.createdAt >= :since ORDER BY t.createdAt DESC")
    List<Todo> findRecentTodos(@Param("since") LocalDateTime since);

    /**
     * Find active todos ordered by priority (HIGH first) and due date
     * @return List of incomplete todos sorted by priority and due date
     */
    @Query("SELECT t FROM Todo t WHERE t.completed = false " +
            "ORDER BY " +
            "CASE t.priority " +
            "WHEN 'HIGH' THEN 1 " +
            "WHEN 'MEDIUM' THEN 2 " +
            "WHEN 'LOW' THEN 3 " +
            "END, " +
            "t.dueDate ASC NULLS LAST")
    List<Todo> findActiveTodosOrderedByPriorityAndDueDate();
}