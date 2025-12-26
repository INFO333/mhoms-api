package com.mhoms.mhomsservices.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI configuration with JWT authentication support
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // Define security scheme for JWT
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("MHOMS API - Medical Hospital Office Management System")
                        .version("1.0.0")
                        .description("""
                                ## 🏥 Hospital Management REST API with JWT Authentication
                                
                                A comprehensive REST API for managing hospital operations including:
                                - **Patient Management**: Register and manage patient records
                                - **Doctor Management**: Manage doctor profiles and specializations
                                - **Appointment Scheduling**: Book, track, and manage appointments
                                - **Authentication & Authorization**: JWT-based security with role-based access
                                
                                ### Features:
                                - ✅ JWT Authentication (Access & Refresh Tokens)
                                - ✅ Role-Based Access Control (ADMIN, DOCTOR, PATIENT)
                                - ✅ Input validation with detailed error messages
                                - ✅ Duplicate appointment prevention
                                - ✅ Comprehensive error handling
                                - ✅ RESTful design principles
                                
                                ### Authentication Flow:
                                1. **Register**: POST /auth/register to create account
                                2. **Login**: POST /auth/login to get JWT tokens
                                3. **Authorize**: Click 🔒 Authorize button, enter: `Bearer <your-access-token>`
                                4. **Use APIs**: All secured endpoints are now accessible
                                
                                ### User Roles & Permissions:
                                - **ADMIN**: Full access to all operations
                                - **DOCTOR**: Manage appointments, view patients
                                - **PATIENT**: Book appointments, view own data
                                
                                ### Tech Stack:
                                - Java 17
                                - Spring Boot 3.2
                                - Spring Security with JWT
                                - PostgreSQL
                                - Hibernate/JPA
                                
                                ### Getting Started:
                                1. Register a user using POST /auth/register
                                2. Login using POST /auth/login
                                3. Copy the accessToken from response
                                4. Click Authorize button and paste: Bearer <token>
                                5. Start using secured endpoints!
                                """)
                        .contact(new Contact()
                                .name("MHOMS Development Team")
                                .email("support@mhoms.com")
                                .url("https://github.com/yourusername/mhoms"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Server"),
                        new Server()
                                .url("https://mhoms-production.com")
                                .description("Production Server (Coming Soon)")
                ))
                // Add JWT security scheme
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT Bearer token in format: Bearer <token>")
                        )
                );
    }
}