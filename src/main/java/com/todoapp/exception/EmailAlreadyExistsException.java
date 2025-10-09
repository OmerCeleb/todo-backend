// src/main/java/com/todoapp/exception/EmailAlreadyExistsException.java
package com.todoapp.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String email) {
        super("Email already exists: " + email);
    }
}
