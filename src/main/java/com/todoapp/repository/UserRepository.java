// src/main/java/com/todoapp/repository/UserRepository.java
package com.todoapp.repository;

import com.todoapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entities.
 * Provides CRUD operations and custom query methods for user management.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email (used for authentication)
     * @param email User's email address
     * @return Optional User entity
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if user exists by email
     * @param email Email to check
     * @return true if user exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Find all active users
     * @param isActive Active status
     * @return List of active users
     */
    List<User> findByIsActive(Boolean isActive);

    /**
     * Find users by role
     * @param role User role (USER, ADMIN)
     * @return List of users with specified role
     */
    List<User> findByRole(User.Role role);

    /**
     * Find users created after a specific date
     * @param date Date threshold
     * @return List of users created after the specified date
     */
    List<User> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Find users by name containing a specific string (case-insensitive)
     * @param name Name or part of name to search for
     * @return List of users whose names contain the search string
     */
    List<User> findByNameContainingIgnoreCase(String name);

    /**
     * Count total number of active users
     * @return Number of active users
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    long countActiveUsers();

    /**
     * Find users with todo count greater than specified value
     * @param todoCount Minimum number of todos
     * @return List of users with more than specified todo count
     */
    @Query("SELECT u FROM User u WHERE SIZE(u.todos) > :todoCount")
    List<User> findUsersWithTodoCountGreaterThan(@Param("todoCount") int todoCount);

    /**
     * Find recently active users (created in last N days)
     * @param sinceDate Date to look back from (LocalDateTime.now().minusDays(N))
     * @return List of recently created users
     */
    @Query("SELECT u FROM User u WHERE u.createdAt >= :sinceDate ORDER BY u.createdAt DESC")
    List<User> findRecentlyCreatedUsers(@Param("sinceDate") LocalDateTime sinceDate);

    /**
     * Find user by email and active status (useful for login)
     * @param email User's email
     * @param isActive Active status
     * @return Optional User entity
     */
    Optional<User> findByEmailAndIsActive(String email, Boolean isActive);

    /**
     * Delete inactive users older than specified date
     * @param date Date threshold
     * @return Number of deleted users
     */
    @Query("DELETE FROM User u WHERE u.isActive = false AND u.createdAt < :date")
    int deleteInactiveUsersBefore(@Param("date") LocalDateTime date);
}