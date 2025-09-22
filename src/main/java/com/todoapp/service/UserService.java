// src/main/java/com/todoapp/service/UserService.java
package com.todoapp.service;

import com.todoapp.dto.auth.UserDTO;
import com.todoapp.entity.User;
import com.todoapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for User management and UserDetailsService implementation.
 * Handles user operations and Spring Security integration.
 */
@Service
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Spring Security UserDetailsService implementation
     * Loads user by username (email in our case)
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailAndIsActive(email, true)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRole().getAuthority())
                .accountExpired(false)
                .accountLocked(!user.getIsActive())
                .credentialsExpired(false)
                .disabled(!user.getIsActive())
                .build();
    }

    /**
     * Create a new user
     * @param name User's name
     * @param email User's email
     * @param rawPassword Raw password (will be encoded)
     * @return Created user DTO
     * @throws RuntimeException if email already exists
     */
    public UserDTO createUser(String name, String email, String rawPassword) {
        // Check if user already exists
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("User already exists with email: " + email);
        }

        // Create new user
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(User.Role.USER);
        user.setIsActive(true);

        User savedUser = userRepository.save(user);
        return new UserDTO(savedUser);
    }

    /**
     * Find user by email
     * @param email User's email
     * @return Optional User entity
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Find active user by email
     * @param email User's email
     * @return Optional User entity
     */
    public Optional<User> findActiveUserByEmail(String email) {
        return userRepository.findByEmailAndIsActive(email, true);
    }

    /**
     * Get user by ID
     * @param id User ID
     * @return User DTO
     * @throws RuntimeException if user not found
     */
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return new UserDTO(user);
    }

    /**
     * Update user information
     * @param id User ID
     * @param name New name
     * @param email New email
     * @return Updated user DTO
     * @throws RuntimeException if user not found or email already exists
     */
    public UserDTO updateUser(Long id, String name, String email) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // Check if email is being changed and if new email already exists
        if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists: " + email);
        }

        user.setName(name);
        user.setEmail(email);

        User updatedUser = userRepository.save(user);
        return new UserDTO(updatedUser);
    }

    /**
     * Change user password
     * @param id User ID
     * @param currentPassword Current password
     * @param newPassword New password
     * @throws RuntimeException if user not found or current password is incorrect
     */
    public void changePassword(Long id, String currentPassword, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Deactivate user account
     * @param id User ID
     * @throws RuntimeException if user not found
     */
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        user.setIsActive(false);
        userRepository.save(user);
    }

    /**
     * Activate user account
     * @param id User ID
     * @throws RuntimeException if user not found
     */
    public void activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        user.setIsActive(true);
        userRepository.save(user);
    }

    /**
     * Get all active users
     * @return List of active user DTOs
     */
    public List<UserDTO> getAllActiveUsers() {
        return userRepository.findByIsActive(true)
                .stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Get users by role
     * @param role User role
     * @return List of user DTOs with specified role
     */
    public List<UserDTO> getUsersByRole(User.Role role) {
        return userRepository.findByRole(role)
                .stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Check if email exists
     * @param email Email to check
     * @return true if email exists, false otherwise
     */
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Get total active user count
     * @return Number of active users
     */
    public long getActiveUserCount() {
        return userRepository.countActiveUsers();
    }

    /**
     * Validate user credentials
     * @param email User's email
     * @param rawPassword Raw password
     * @return true if credentials are valid, false otherwise
     */
    public boolean validateCredentials(String email, String rawPassword) {
        Optional<User> userOpt = findActiveUserByEmail(email);
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    /**
     * Search users by name
     * @param name Name or part of name to search
     * @return List of matching user DTOs
     */
    public List<UserDTO> searchUsersByName(String name) {
        return userRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
    }
}