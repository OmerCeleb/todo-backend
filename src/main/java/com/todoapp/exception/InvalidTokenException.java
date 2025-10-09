// src/main/java/com/todoapp/exception/InvalidTokenException.java
package com.todoapp.exception;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }
}