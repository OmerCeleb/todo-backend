package com.todoapp.dto.auth;

import com.todoapp.entity.User;

/**
 * DTO for authentication response (login/register success)
 */
public class AuthResponseDTO {

    private String token;
    private String refreshToken;
    private String type = "Bearer";
    private UserDTO user;

    // Constructors
    public AuthResponseDTO() {}

    public AuthResponseDTO(String token, String refreshToken, UserDTO user) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.user = user;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }
}
