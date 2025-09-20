package com.todoapp.dto;

import com.todoapp.entity.Todo;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for outgoing Todo responses (GET operations).
 * Contains all todo information including system-generated fields.
 */
public class TodoResponseDTO {

    private Long id;
    private String title;
    private String description;
    private boolean completed;
    private Todo.Priority priority;
    private String category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime dueDate;

    /**
     * Default constructor
     */
    public TodoResponseDTO() {}

    /**
     * Constructor that converts Todo entity to DTO
     * @param todo Todo entity to convert
     */
    public TodoResponseDTO(Todo todo) {
        this.id = todo.getId();
        this.title = todo.getTitle();
        this.description = todo.getDescription();
        this.completed = todo.isCompleted();
        this.priority = todo.getPriority();
        this.category = todo.getCategory();
        this.createdAt = todo.getCreatedAt();
        this.updatedAt = todo.getUpdatedAt();
        this.dueDate = todo.getDueDate();
    }

    // Getters and Setters

    /**
     * Get the todo ID
     * @return todo ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Set the todo ID
     * @param id todo ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Get the todo title
     * @return todo title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set the todo title
     * @param title todo title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get the todo description
     * @return todo description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the todo description
     * @param description todo description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Check if todo is completed
     * @return true if completed, false otherwise
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Set todo completion status
     * @param completed completion status
     */
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    /**
     * Get the todo priority
     * @return todo priority
     */
    public Todo.Priority getPriority() {
        return priority;
    }

    /**
     * Set the todo priority
     * @param priority todo priority
     */
    public void setPriority(Todo.Priority priority) {
        this.priority = priority;
    }

    /**
     * Get the todo category
     * @return todo category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Set the todo category
     * @param category todo category
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Get the creation timestamp
     * @return creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Set the creation timestamp
     * @param createdAt creation timestamp
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Get the last update timestamp
     * @return last update timestamp
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Set the last update timestamp
     * @param updatedAt last update timestamp
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Get the due date
     * @return due date
     */
    public LocalDateTime getDueDate() {
        return dueDate;
    }

    /**
     * Set the due date
     * @param dueDate due date
     */
    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    @Override
    public String toString() {
        return "TodoResponseDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", completed=" + completed +
                ", priority=" + priority +
                ", category='" + category + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}