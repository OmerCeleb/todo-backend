// src/test/java/com/todoapp/service/TodoServiceTest.java
package com.todoapp.service;

import com.todoapp.dto.TodoRequestDTO;
import com.todoapp.dto.TodoResponseDTO;
import com.todoapp.entity.Todo;
import com.todoapp.entity.User;
import com.todoapp.repository.TodoRepository;
import com.todoapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TodoService Tests")
class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TodoService todoService;

    private User testUser;
    private Todo testTodo;
    private TodoRequestDTO todoRequestDTO;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword");

        // Setup test todo
        testTodo = new Todo();
        testTodo.setId(1L);
        testTodo.setTitle("Test Todo");
        testTodo.setDescription("Test Description");
        testTodo.setCompleted(false);
        testTodo.setPriority(Todo.Priority.HIGH);
        testTodo.setCategory("Work");
        testTodo.setUser(testUser);
        testTodo.setCreatedAt(LocalDateTime.now());
        testTodo.setUpdatedAt(LocalDateTime.now());

        // Setup test DTO
        todoRequestDTO = new TodoRequestDTO();
        todoRequestDTO.setTitle("New Todo");
        todoRequestDTO.setDescription("New Description");
        todoRequestDTO.setPriority(Todo.Priority.MEDIUM);
        todoRequestDTO.setCategory("Personal");
    }

    @Test
    @DisplayName("Should get user todos successfully")
    void testGetUserTodos() {
        // Given
        List<Todo> todos = Arrays.asList(testTodo);
        when(todoRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(todos);

        // When
        List<TodoResponseDTO> result = todoService.getUserTodos(1L, null, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Todo", result.get(0).getTitle());
        verify(todoRepository, times(1)).findByUserIdOrderByCreatedAtDesc(1L);
    }

    @Test
    @DisplayName("Should get todo by id successfully")
    void testGetUserTodoById() {
        // Given
        when(todoRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testTodo));

        // When
        Optional<TodoResponseDTO> result = todoService.getUserTodoById(1L, 1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Test Todo", result.get().getTitle());
        verify(todoRepository, times(1)).findByIdAndUserId(1L, 1L);
    }

    @Test
    @DisplayName("Should toggle todo completion")
    void testToggleTodo() {
        // Given
        when(todoRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testTodo));
        when(todoRepository.save(any(Todo.class))).thenReturn(testTodo);

        // When
        Optional<TodoResponseDTO> result = todoService.toggleUserTodoCompletion(1L, 1L);

        // Then
        assertTrue(result.isPresent());
        verify(todoRepository, times(1)).save(any(Todo.class));
    }

    @Test
    @DisplayName("Should return empty when todo not found")
    void testGetTodoByIdNotFound() {
        // Given
        when(todoRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());

        // When
        Optional<TodoResponseDTO> result = todoService.getUserTodoById(999L, 1L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should filter todos by priority")
    void testGetUserTodosWithPriorityFilter() {
        // Given
        List<Todo> todos = Arrays.asList(testTodo);
        when(todoRepository.findByUserIdWithFilters(1L, null, Todo.Priority.HIGH, null))
                .thenReturn(todos);

        // When
        List<TodoResponseDTO> result = todoService.getUserTodos(1L, null, Todo.Priority.HIGH, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Todo.Priority.HIGH, result.get(0).getPriority());
    }

    @Test
    @DisplayName("Should search todos by title")
    void testSearchTodos() {
        // Given
        List<Todo> todos = Arrays.asList(testTodo);
        when(todoRepository.findByUserIdAndTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                1L, "Test", "Test")).thenReturn(todos);

        // When
        List<TodoResponseDTO> result = todoService.getUserTodos(1L, null, null, null, "Test");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should bulk delete todos")
    void testBulkDeleteTodos() {
        // Given
        List<Long> ids = Arrays.asList(1L, 2L);
        when(todoRepository.findAllById(ids)).thenReturn(Arrays.asList(testTodo));

        // When
        int deletedCount = todoService.bulkDeleteUserTodos(1L, ids);

        // Then
        assertEquals(1, deletedCount);
        verify(todoRepository, times(1)).deleteAll(anyList());
    }

    @Test
    @DisplayName("Should get user statistics")
    void testGetUserTodoStats() {
        // Given
        List<Todo> todos = Arrays.asList(testTodo);
        when(todoRepository.findByUserId(1L)).thenReturn(todos);

        // When
        var stats = todoService.getUserTodoStats(1L);

        // Then
        assertNotNull(stats);
        assertEquals(1, stats.getTotal());
        assertEquals(0, stats.getCompleted());
        assertEquals(1, stats.getActive());
    }

    @Test
    @DisplayName("Should create todo for user")
    void testCreateTodoForUser() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(todoRepository.save(any(Todo.class))).thenReturn(testTodo);

        // When
        TodoResponseDTO result = todoService.createTodoForUser(1L, todoRequestDTO);

        // Then
        assertNotNull(result);
        verify(todoRepository, times(1)).save(any(Todo.class));
    }

    @Test
    @DisplayName("Should update user todo")
    void testUpdateUserTodo() {
        // Given
        when(todoRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testTodo));
        when(todoRepository.save(any(Todo.class))).thenReturn(testTodo);

        // When
        Optional<TodoResponseDTO> result = todoService.updateUserTodo(1L, 1L, todoRequestDTO);

        // Then
        assertTrue(result.isPresent());
        verify(todoRepository, times(1)).save(any(Todo.class));
    }

    @Test
    @DisplayName("Should delete user todo")
    void testDeleteUserTodo() {
        // Given
        when(todoRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testTodo));

        // When
        boolean result = todoService.deleteUserTodo(1L, 1L);

        // Then
        assertTrue(result);
        verify(todoRepository, times(1)).delete(any(Todo.class));
    }
}