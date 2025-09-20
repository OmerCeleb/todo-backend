package com.todoapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TodoAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(TodoAppApplication.class, args);
        System.out.println("🚀 Todo App Backend started successfully!");
        System.out.println("📍 Server running on: http://localhost:8080");
        System.out.println("📊 API Documentation: http://localhost:8080/api/todos");
    }
}