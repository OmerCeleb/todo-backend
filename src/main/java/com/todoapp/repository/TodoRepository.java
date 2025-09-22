// src/main/java/com/todoapp/repository/TodoRepository.java
package com.todoapp.repository;

import com.todoapp.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Todo entities with user authentication support.
 * Extends JpaRepository to provide CRUD operations and custom query methods.
 */
@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {

    // ================================================
    // USER-SPECIFIC METHODS (NEW)
    // ================================================

    /**
     * Find all todos for a specific user
     */
    List<Todo> findByUserId(Long userId);

    /**
     * Find all todos for a user ordered by creation date (newest first)
     */
    List<Todo> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find a specific todo by ID and user ID (security check)
     */
    Optional<Todo> findByIdAndUserId(Long id, Long userId);

    /**
     * Find todos by user and completion status
     */
    List<Todo> findByUserIdAndCompleted(Long userId, boolean completed);

    /**
     * Find todos by user and priority
     */
    List<Todo> findByUserIdAndPriority(Long userId, Todo.Priority priority);

    /**
     * Find todos by user and category
     */
    List<Todo> findByUserIdAndCategory(Long userId, String category);

    /**
     * Find todos by user and category (case-insensitive)
     */
    List<Todo> findByUserIdAndCategoryIgnoreCase(Long userId, String category);

    /**
     * Find overdue todos for a specific user
     */
    List<Todo> findByUserIdAndDueDateBeforeAndCompletedFalse(Long userId, LocalDateTime date);

    /**
     * Find todos by user with title or description search
     */
    List<Todo> findByUserIdAndTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            Long userId, String titleSearch, String descriptionSearch);

    /**
     * Find todos by user with due date range
     */
    List<Todo> findByUserIdAndDueDateBetween(Long userId, LocalDateTime start, LocalDateTime end);

    /**
     * Complex filtering query for user todos
     */
    @Query("SELECT t FROM Todo t WHERE t.user.id = :userId " +
            "AND (:completed IS NULL OR t.completed = :completed) " +
            "AND (:priority IS NULL OR t.priority = :priority) " +
            "AND (:category IS NULL OR t.category = :category) " +
            "ORDER BY t.createdAt DESC")
    List<Todo> findByUserIdWithFilters(@Param("userId") Long userId,
                                       @Param("completed") Boolean completed,
                                       @Param("priority") Todo.Priority priority,
                                       @Param("category") String category);

    /**
     * Get distinct categories for a user
     */
    @Query("SELECT DISTINCT t.category FROM Todo t WHERE t.user.id = :userId AND t.category IS NOT NULL ORDER BY t.category")
    List<String> findDistinctCategoriesByUserId(@Param("userId") Long userId);

    /**
     * Count todos by user and completion status
     */
    long countByUserIdAndCompleted(Long userId, boolean completed);

    /**
     * Count overdue todos for a user
     */
    @Query("SELECT COUNT(t) FROM Todo t WHERE t.user.id = :userId AND t.dueDate < :date AND t.completed = false")
    long countOverdueByUserId(@Param("userId") Long userId, @Param("date") LocalDateTime date);

    // ================================================
    // ORIGINAL METHODS (KEPT FOR BACKWARD COMPATIBILITY)
    // ================================================

    /**
     * Find todos by completion status
     */
    List<Todo> findByCompleted(boolean completed);

    /**
     * Find todos by priority level
     */
    List<Todo> findByPriority(Todo.Priority priority);

    /**
     * Find todos by exact category match
     */
    List<Todo> findByCategory(String category);

    /**
     * Find todos by category (case-insensitive)
     */
    List<Todo> findByCategoryIgnoreCase(String category);

    /**
     * Find overdue todos (due date has passed and not completed)
     */
    List<Todo> findByDueDateBeforeAndCompletedFalse(LocalDateTime date);

    /**
     * Find todos with due dates within a specific time range
     */
    List<Todo> findByDueDateBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Search todos by title or description containing search term (case-insensitive)
     */
    List<Todo> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String titleSearch, String descriptionSearch);

    /**
     * Find todos by priority and completion status
     */
    List<Todo> findByPriorityAndCompleted(Todo.Priority priority, boolean completed);

    /**
     * Find todos due today - use parameterized query instead
     */
    @Query("SELECT t FROM Todo t WHERE t.dueDate >= :startOfDay AND t.dueDate < :endOfDay")
    List<Todo> findTodosDueToday(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * Find active todos ordered by priority (HIGH first) and due date
     */
    List<Todo> findByCompletedFalseOrderByPriorityDescDueDateAsc();

    /**
     * Find todos created after a specific date
     */
    List<Todo> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Find todos updated after a specific date
     */
    List<Todo> findByUpdatedAtAfter(LocalDateTime date);

    /**
     * Get all distinct categories (non-null)
     */
    @Query("SELECT DISTINCT t.category FROM Todo t WHERE t.category IS NOT NULL ORDER BY t.category")
    List<String> findDistinctCategories();

    /**
     * Find recently completed todos (completed within last N days)
     */
    @Query("SELECT t FROM Todo t WHERE t.completed = true AND t.completedAt >= :since ORDER BY t.completedAt DESC")
    List<Todo> findRecentlyCompleted(@Param("since") LocalDateTime since);

    /**
     * Find todos by multiple priorities
     */
    List<Todo> findByPriorityIn(List<Todo.Priority> priorities);

    /**
     * Count todos by priority
     */
    long countByPriority(Todo.Priority priority);

    /**
     * Count completed todos
     */
    long countByCompleted(boolean completed);

    /**
     * Count todos in specific category
     */
    long countByCategory(String category);

    /**
     * Find todos with no due date
     */
    List<Todo> findByDueDateIsNull();

    /**
     * Find todos with due date set
     */
    List<Todo> findByDueDateIsNotNull();

    /**
     * Advanced search with multiple criteria
     */
    @Query("SELECT t FROM Todo t WHERE " +
            "(:searchTerm IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "AND (:completed IS NULL OR t.completed = :completed) " +
            "AND (:priority IS NULL OR t.priority = :priority) " +
            "AND (:category IS NULL OR t.category = :category) " +
            "ORDER BY t.createdAt DESC")
    List<Todo> findWithAdvancedSearch(@Param("searchTerm") String searchTerm,
                                      @Param("completed") Boolean completed,
                                      @Param("priority") Todo.Priority priority,
                                      @Param("category") String category);

    /**
     * Delete completed todos older than specified date
     */
    @Query("DELETE FROM Todo t WHERE t.completed = true AND t.completedAt < :date")
    int deleteCompletedBefore(@Param("date") LocalDateTime date);

    /**
     * Find todos by title (exact match, case-insensitive)
     */
    Optional<Todo> findByTitleIgnoreCase(String title);

    /**
     * Check if todo exists with same title for user (to prevent duplicates)
     */
    boolean existsByUserIdAndTitleIgnoreCase(Long userId, String title);
}