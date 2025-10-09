// src/main/java/com/todoapp/config/OpenAPIConfig.java
package com.todoapp.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for API documentation
 * Access documentation at: http://localhost:8080/swagger-ui.html
 */
@Configuration
public class OpenAPIConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI todoAppOpenAPI() {
        // Security scheme for JWT
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("Enter JWT token (without 'Bearer ' prefix)");

        // Security requirement
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("bearerAuth");

        // Server configuration
        Server localServer = new Server()
                .url("http://localhost:" + serverPort)
                .description("Local Development Server");

        Server productionServer = new Server()
                .url("https://api.todoapp.com")
                .description("Production Server");

        // API Info
        Info apiInfo = new Info()
                .title("Todo Application API")
                .description("""
                        ## Modern Todo Application REST API
                        
                        A comprehensive todo management system with user authentication.
                        
                        ### Features:
                        - 🔐 JWT-based authentication
                        - ✅ Complete CRUD operations for todos
                        - 👤 User management and profiles
                        - 🏷️ Category and priority management
                        - 📊 Statistics and analytics
                        - 🔍 Advanced filtering and search
                        
                        ### Authentication:
                        1. Register a new user or login with existing credentials
                        2. Copy the JWT token from the response
                        3. Click 'Authorize' button and paste the token
                        4. Now you can access protected endpoints
                        
                        ### Getting Started:
                        1. **POST /api/auth/register** - Create a new account
                        2. **POST /api/auth/login** - Get your JWT token
                        3. **GET /api/todos** - Fetch your todos
                        4. **POST /api/todos** - Create a new todo
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("Ömer Çelebi")
                        .email("omer534@outlook.com")
                        .url("https://www.linkedin.com/in/omercelebii/"))
                .license(new License()
                        .name("MIT License")
                        .url("https://github.com/OmerCeleb/todo-backend/blob/main/LICENSE"));

        return new OpenAPI()
                .info(apiInfo)
                .servers(List.of(localServer, productionServer))
                .addSecurityItem(securityRequirement)
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", securityScheme));
    }
}