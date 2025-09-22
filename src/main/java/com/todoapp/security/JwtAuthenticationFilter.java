// src/main/java/com/todoapp/security/JwtAuthenticationFilter.java
package com.todoapp.security;

import com.todoapp.service.UserService;
import com.todoapp.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter that intercepts requests to validate JWT tokens.
 * Extends OncePerRequestFilter to ensure it's executed once per request.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Autowired
    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            // Get JWT token from request header
            String jwt = getJwtFromRequest(request);

            // If JWT exists and is valid
            if (jwt != null && jwtUtil.validateToken(jwt)) {
                // Extract username from token
                String email = jwtUtil.extractUsername(jwt);

                // Load user details
                UserDetails userDetails = userService.loadUserByUsername(email);

                // Validate token against user details
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    // Create authentication token
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    // Set authentication details
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Set authentication in security context
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception ex) {
            // Log error but don't block the request
            logger.error("Could not set user authentication in security context", ex);
        }

        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from the Authorization header
     * @param request HTTP request
     * @return JWT token string or null if not found
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        // Check if the authorization header starts with "Bearer "
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Remove "Bearer " prefix
        }

        return null;
    }

    /**
     * Determine if this filter should be applied to the current request.
     * Skip authentication for public endpoints.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        // Skip authentication for these public endpoints
        return path.startsWith("/api/auth/login") ||
                path.startsWith("/api/auth/register") ||
                path.startsWith("/api/auth/refresh") ||
                path.startsWith("/api/auth/validate") ||
                path.startsWith("/h2-console") ||
                path.startsWith("/actuator") ||
                path.equals("/") ||
                path.startsWith("/static/") ||
                path.startsWith("/public/") ||
                path.endsWith(".html") ||
                path.endsWith(".css") ||
                path.endsWith(".js") ||
                path.endsWith(".ico");
    }
}