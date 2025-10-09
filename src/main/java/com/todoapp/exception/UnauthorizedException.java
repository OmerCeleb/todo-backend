// src/main/java/com/todoapp/exception/UnauthorizedException.java
package com.todoapp.exception;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}