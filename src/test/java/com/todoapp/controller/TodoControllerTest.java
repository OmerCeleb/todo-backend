// src/test/java/com/todoapp/controller/TodoControllerTest.java
package com.todoapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todoapp.dto.TodoRequestDTO;
import com.todoapp.entity.Todo;
import com.todoapp.entity.User;
import com.todoapp.repository.TodoRepository;
import com.todoapp.repository.UserRepository;
import com.todoapp.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("TodoController Integration Tests")
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private String jwtToken;
    private Todo testTodo;

    @BeforeEach
    void setUp() {
        // Clean database
        todoRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRole(User.Role.USER);
        testUser.setIsActive(true);
        testUser = userRepository.save(testUser);

        // Generate JWT token
        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User(
                        testUser.getEmail(),
                        testUser.getPassword(),
                        java.util.Collections.singletonList(
                                new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                        testUser.getRole().getAuthority()
                                )
                        )
                );
        jwtToken = jwtUtil.generateToken(userDetails);

        // Create test todo
        testTodo = new Todo();
        testTodo.setTitle("Test Todo");
        testTodo.setDescription("Test Description");
        testTodo.setCompleted(false);
        testTodo.setPriority(Todo.Priority.HIGH);
        testTodo.setCategory("Work");
        testTodo.setUser(testUser);
        testTodo = todoRepository.save(testTodo);
    }

    @Test
    @DisplayName("Should get all user todos")
    void testGetAllUserTodos() throws Exception {
        mockMvc.perform(get("/api/todos")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Test Todo")))
                .andExpect(jsonPath("$[0].description", is("Test Description")));
    }

    @Test
    @DisplayName("Should create new todo")
    void testCreateTodo() throws Exception {
        TodoRequestDTO newTodo = new TodoRequestDTO();
        newTodo.setTitle("New Todo");
        newTodo.setDescription("New Description");
        newTodo.setPriority(Todo.Priority.MEDIUM);
        newTodo.setCategory("Personal");

        mockMvc.perform(post("/api/todos")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTodo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("New Todo")))
                .andExpect(jsonPath("$.priority", is("MEDIUM")));
    }

    @Test
    @DisplayName("Should get todo by id")
    void testGetTodoById() throws Exception {
        mockMvc.perform(get("/api/todos/" + testTodo.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Test Todo")));
    }

    @Test
    @DisplayName("Should update todo")
    void testUpdateTodo() throws Exception {
        TodoRequestDTO updateDTO = new TodoRequestDTO();
        updateDTO.setTitle("Updated Todo");
        updateDTO.setDescription("Updated Description");
        updateDTO.setPriority(Todo.Priority.LOW);

        mockMvc.perform(put("/api/todos/" + testTodo.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated Todo")))
                .andExpect(jsonPath("$.priority", is("LOW")));
    }

    @Test
    @DisplayName("Should delete todo")
    void testDeleteTodo() throws Exception {
        mockMvc.perform(delete("/api/todos/" + testTodo.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("successfully")));
    }

    @Test
    @DisplayName("Should toggle todo completion")
    void testToggleTodo() throws Exception {
        mockMvc.perform(patch("/api/todos/" + testTodo.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed", is(true)));
    }

    @Test
    @DisplayName("Should return 401 without authentication")
    void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/api/todos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should validate todo title is required")
    void testValidationTitleRequired() throws Exception {
        TodoRequestDTO invalidTodo = new TodoRequestDTO();
        invalidTodo.setDescription("Description only");

        mockMvc.perform(post("/api/todos")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTodo)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should filter todos by priority")
    void testFilterByPriority() throws Exception {
        mockMvc.perform(get("/api/todos")
                        .param("priority", "HIGH")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].priority", is("HIGH")));
    }

    @Test
    @DisplayName("Should get todo statistics")
    void testGetStats() throws Exception {
        mockMvc.perform(get("/api/todos/stats")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(1)))
                .andExpect(jsonPath("$.active", is(1)))
                .andExpect(jsonPath("$.completed", is(0)));
    }
}