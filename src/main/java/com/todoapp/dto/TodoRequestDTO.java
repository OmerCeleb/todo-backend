// src/main/java/com/todoapp/dto/TodoRequestDTO.java
package com.todoapp.dto;

import com.todoapp.entity.Todo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for incoming Todo requests (POST/PUT operations).
 * Contains validation annotations to ensure data integrity.
 */
public class TodoRequestDTO {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be less than 255 characters")
    private String title;

    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;

    private Boolean completed; // Added completed field

    private Todo.Priority priority = Todo.Priority.MEDIUM;

    @Size(max = 100, message = "Category must be less than 100 characters")
    private String category;

    private LocalDateTime dueDate;

    /**
     * Default constructor
     */
    public TodoRequestDTO() {}

    /**
     * Constructor with all fields
     * @param title Todo title
     * @param description Todo description
     * @param completed Todo completion status
     * @param priority Todo priority level
     * @param category Todo category
     * @param dueDate Todo due date
     */
    public TodoRequestDTO(String title, String description, Boolean completed, Todo.Priority priority, String category, LocalDateTime dueDate) {
        this.title = title;
        this.description = description;
        this.completed = completed;
        this.priority = priority;
        this.category = category;
        this.dueDate = dueDate;
    }

    // Getters and Setters

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
     * Get the todo completion status
     * @return completion status (null if not specified)
     */
    public Boolean getCompleted() {
        return completed;
    }

    /**
     * Set the todo completion status
     * @param completed completion status
     */
    public void setCompleted(Boolean completed) {
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
     * Get the todo due date
     * @return todo due date
     */
    public LocalDateTime getDueDate() {
        return dueDate;
    }

    /**
     * Set the todo due date
     * @param dueDate todo due date
     */
    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    @Override
    public String toString() {
        return "TodoRequestDTO{" +
                "title='" + title + '\'' +
                ", completed=" + completed +
                ", priority=" + priority +
                ", category='" + category + '\'' +
                ", dueDate=" + dueDate +
                '}';
    }
}