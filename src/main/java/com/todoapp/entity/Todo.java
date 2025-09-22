// src/main/java/com/todoapp/entity/Todo.java
package com.todoapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Todo entity representing a task or todo item.
 * Each todo belongs to a user and contains task information.
 */
@Entity
@Table(name = "todos", indexes = {
        @Index(name = "idx_todo_user", columnList = "user_id"),
        @Index(name = "idx_todo_completed", columnList = "completed"),
        @Index(name = "idx_todo_priority", columnList = "priority"),
        @Index(name = "idx_todo_due_date", columnList = "due_date")
})
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    @Column(nullable = false)
    private String title;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Boolean completed = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority = Priority.MEDIUM;

    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // ============================================
    // USER RELATIONSHIP - ADDED FOR AUTHENTICATION
    // ============================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Priority levels for todos
     */
    public enum Priority {
        LOW("Low"),
        MEDIUM("Medium"),
        HIGH("High");

        private final String displayName;

        Priority(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Constructors
    public Todo() {}

    public Todo(String title, String description, User user) {
        this.title = title;
        this.description = description;
        this.user = user;
    }

    public Todo(String title, String description, Priority priority, String category, User user) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.category = category;
        this.user = user;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
        // Set completion timestamp when marking as completed
        if (completed && this.completedAt == null) {
            this.completedAt = LocalDateTime.now();
        } else if (!completed) {
            this.completedAt = null;
        }
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // Utility methods

    /**
     * Check if todo is overdue
     */
    public boolean isOverdue() {
        return dueDate != null && !completed && LocalDateTime.now().isAfter(dueDate);
    }

    /**
     * Check if todo is due today
     */
    public boolean isDueToday() {
        if (dueDate == null) return false;
        LocalDateTime now = LocalDateTime.now();
        return dueDate.toLocalDate().equals(now.toLocalDate());
    }

    /**
     * Check if todo is due within the next 24 hours
     */
    public boolean isDueSoon() {
        if (dueDate == null) return false;
        LocalDateTime now = LocalDateTime.now();
        return dueDate.isAfter(now) && dueDate.isBefore(now.plusDays(1));
    }

    /**
     * Mark todo as completed
     */
    public void markAsCompleted() {
        this.completed = true;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * Mark todo as incomplete
     */
    public void markAsIncomplete() {
        this.completed = false;
        this.completedAt = null;
    }

    /**
     * Toggle completion status
     */
    public void toggleCompleted() {
        if (this.completed) {
            markAsIncomplete();
        } else {
            markAsCompleted();
        }
    }

    // toString method (excluding user to avoid circular reference)
    @Override
    public String toString() {
        return "Todo{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", completed=" + completed +
                ", priority=" + priority +
                ", category='" + category + '\'' +
                ", dueDate=" + dueDate +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", completedAt=" + completedAt +
                ", userId=" + (user != null ? user.getId() : null) +
                '}';
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Todo)) return false;
        Todo todo = (Todo) o;
        return id != null && id.equals(todo.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}