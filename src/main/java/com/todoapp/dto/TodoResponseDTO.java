// src/main/java/com/todoapp/dto/TodoResponseDTO.java
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
    private LocalDateTime completedAt;
    private Integer displayOrder;  // NEW FIELD

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
        this.completed = todo.getCompleted();
        this.priority = todo.getPriority();
        this.category = todo.getCategory();
        this.createdAt = todo.getCreatedAt();
        this.updatedAt = todo.getUpdatedAt();
        this.dueDate = todo.getDueDate();
        this.completedAt = todo.getCompletedAt();
        this.displayOrder = todo.getDisplayOrder();  // NEW
    }

    // Existing getters and setters...

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

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Todo.Priority getPriority() {
        return priority;
    }

    public void setPriority(Todo.Priority priority) {
        this.priority = priority;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    // NEW GETTER/SETTER
    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    @Override
    public String toString() {
        return "TodoResponseDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", completed=" + completed +
                ", priority=" + priority +
                ", category='" + category + '\'' +
                ", displayOrder=" + displayOrder +
                ", createdAt=" + createdAt +
                ", dueDate=" + dueDate +
                '}';
    }
}