// src/main/java/com/todoapp/exception/TodoNotFoundException.java
package com.todoapp.exception;

public class TodoNotFoundException extends RuntimeException {
    public TodoNotFoundException(String message) {
        super(message);
    }

    public TodoNotFoundException(Long id) {
        super("Todo not found with id: " + id);
    }
}






