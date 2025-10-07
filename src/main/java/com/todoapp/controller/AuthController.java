// src/main/java/com/todoapp/controller/AuthController.java
package com.todoapp.controller;

import com.todoapp.dto.auth.*;
import com.todoapp.entity.User;
import com.todoapp.service.UserService;
import com.todoapp.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for authentication endpoints.
 * Handles user registration, login, token refresh, and logout operations.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                          UserService userService,
                          JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Register a new user
     * @param registerRequest User registration data
     * @return Authentication response with JWT token
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        try {
            // Check if user already exists
            if (userService.emailExists(registerRequest.getEmail())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Email already exists!");
                return ResponseEntity.badRequest().body(error);
            }

            // Create new user
            UserDTO newUser = userService.createUser(
                    registerRequest.getName(),
                    registerRequest.getEmail(),
                    registerRequest.getPassword()
            );

            // Generate tokens
            UserDetails userDetails = userService.loadUserByUsername(registerRequest.getEmail());
            String accessToken = jwtUtil.generateToken(userDetails);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);

            // Return success response
            AuthResponseDTO response = new AuthResponseDTO(accessToken, refreshToken, newUser);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Registration failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Login user
     * @param loginRequest User login credentials
     * @return Authentication response with JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            // Get user details
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Optional<User> userOpt = userService.findActiveUserByEmail(loginRequest.getEmail());

            if (userOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "User not found or inactive");
                return ResponseEntity.badRequest().body(error);
            }

            User user = userOpt.get();
            UserDTO userDTO = new UserDTO(user);

            // Generate tokens
            String accessToken = jwtUtil.generateToken(userDetails);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);

            // Return success response
            AuthResponseDTO response = new AuthResponseDTO(accessToken, refreshToken, userDTO);
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Invalid email or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Login failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Refresh access token using refresh token
     * @param refreshRequest Refresh token request
     * @return New access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO refreshRequest) {
        try {
            String refreshToken = refreshRequest.getRefreshToken();

            // Validate refresh token
            if (!jwtUtil.validateToken(refreshToken)) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Invalid refresh token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            // Check if it's actually a refresh token
            if (!jwtUtil.isRefreshToken(refreshToken)) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Token is not a refresh token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            // Extract username and generate new access token
            String email = jwtUtil.extractUsername(refreshToken);
            UserDetails userDetails = userService.loadUserByUsername(email);
            String newAccessToken = jwtUtil.generateToken(userDetails);

            // Get user info
            Optional<User> userOpt = userService.findActiveUserByEmail(email);
            if (userOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "User not found or inactive");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            UserDTO userDTO = new UserDTO(userOpt.get());

            // Return new tokens
            AuthResponseDTO response = new AuthResponseDTO(newAccessToken, refreshToken, userDTO);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Token refresh failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    /**
     * Logout user (invalidate token - client-side implementation)
     * @return Success message
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        // In a more sophisticated implementation, you might want to:
        // 1. Maintain a blacklist of tokens
        // 2. Store tokens in Redis with expiration
        // 3. Invalidate refresh tokens in database

        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Validate JWT token
     * @param token JWT token to validate
     * @return Validation result
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        try {
            boolean isValid = jwtUtil.validateToken(token);

            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);

            if (isValid) {
                String email = jwtUtil.extractUsername(token);
                Optional<User> userOpt = userService.findActiveUserByEmail(email);
                if (userOpt.isPresent()) {
                    response.put("user", new UserDTO(userOpt.get()));
                }
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Get current user profile
     * @param email User email from JWT token (set by authentication filter)
     * @return User profile information
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestParam String email) {
        try {
            Optional<User> userOpt = userService.findActiveUserByEmail(email);
            if (userOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            UserDTO userDTO = new UserDTO(userOpt.get());
            return ResponseEntity.ok(userDTO);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to get user profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Change user password
     * @param email User email from JWT token
     * @param passwordRequest Password change request
     * @return Success message
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestParam String email,
                                            @RequestBody Map<String, String> passwordRequest) {
        try {
            String currentPassword = passwordRequest.get("currentPassword");
            String newPassword = passwordRequest.get("newPassword");

            if (currentPassword == null || newPassword == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Current password and new password are required");
                return ResponseEntity.badRequest().body(error);
            }

            Optional<User> userOpt = userService.findActiveUserByEmail(email);
            if (userOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            userService.changePassword(userOpt.get().getId(), currentPassword, newPassword);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Password changed successfully");
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Password change failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}