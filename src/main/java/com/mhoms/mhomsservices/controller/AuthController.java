package com.mhoms.mhomsservices.controller;

import com.mhoms.mhomsservices.dto.AuthResponse;
import com.mhoms.mhomsservices.dto.LoginRequest;
import com.mhoms.mhomsservices.dto.RefreshTokenRequest;
import com.mhoms.mhomsservices.dto.RegisterRequest;
import com.mhoms.mhomsservices.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for authentication endpoints
 * Handles user registration, login, and token refresh
 */
@RestController
@RequestMapping("/auth")
@Tag(
        name = "Authentication",
        description = "APIs for user authentication including registration, login, and token refresh"
)
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account with specified role (ADMIN, DOCTOR, or PATIENT). Password is encrypted before storage. Returns JWT access and refresh tokens."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid registration data",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Username or email already exists",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User registration details",
                    required = true,
                    content = @Content(
                            schema = @Schema(
                                    implementation = RegisterRequest.class,
                                    example = """
                        {
                          "username": "john_admin",
                          "email": "john@mhoms.com",
                          "password": "password123",
                          "fullName": "John Admin",
                          "role": "ADMIN"
                        }
                        """
                            )
                    )
            )
            @Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @Operation(
            summary = "Login user",
            description = "Authenticates user with username and password. Returns JWT access token (valid for 24 hours) and refresh token (valid for 7 days)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid username or password",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PostMapping("/login")
    public AuthResponse login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Login credentials",
                    required = true,
                    content = @Content(
                            schema = @Schema(
                                    implementation = LoginRequest.class,
                                    example = """
                        {
                          "username": "john_admin",
                          "password": "password123"
                        }
                        """
                            )
                    )
            )
            @Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @Operation(
            summary = "Refresh access token",
            description = "Generates a new access token using a valid refresh token. Use this when the access token expires to avoid re-login."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token refreshed successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid or expired refresh token",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PostMapping("/refresh")
    public AuthResponse refreshToken(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Refresh token to generate new access token",
                    required = true,
                    content = @Content(
                            schema = @Schema(
                                    implementation = RefreshTokenRequest.class,
                                    example = """
                        {
                          "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                        }
                        """
                            )
                    )
            )
            @Valid @RequestBody RefreshTokenRequest request) {
        return authService.refreshToken(request.getRefreshToken());
    }
}