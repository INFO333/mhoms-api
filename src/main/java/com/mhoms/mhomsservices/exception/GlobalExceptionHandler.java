package com.mhoms.mhomsservices.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for all REST controllers
 * Provides user-friendly error messages for different exception types
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ═══════════════════════════════════════════════════════
    // SECURITY & AUTHENTICATION EXCEPTIONS
    // ═══════════════════════════════════════════════════════

    /**
     * Handle 403 - Access Denied (Forbidden)
     * Triggered when authenticated user doesn't have required role/permission
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            AccessDeniedException ex) {

        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", 403);
        error.put("error", "Forbidden");
        error.put("message", "Access Denied - You don't have permission to perform this action");
        error.put("details", "This operation requires higher privileges. Contact administrator if you believe this is an error.");

        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle 401 - Bad Credentials (Wrong username/password)
     * Triggered during login with incorrect credentials
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(
            BadCredentialsException ex) {

        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", 401);
        error.put("error", "Unauthorized");
        error.put("message", "Invalid username or password");
        error.put("details", "Please check your credentials and try again.");

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle 401 - User Not Found
     * Triggered when username doesn't exist
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUsernameNotFound(
            UsernameNotFoundException ex) {

        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", 401);
        error.put("error", "Unauthorized");
        error.put("message", "User not found");
        error.put("details", "The username you entered doesn't exist. Please register first.");

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle 401 - Generic Authentication Exception
     * Catches any other authentication-related errors
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(
            AuthenticationException ex) {

        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", 401);
        error.put("error", "Unauthorized");
        error.put("message", "Authentication failed");
        error.put("details", ex.getMessage());

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    // ═══════════════════════════════════════════════════════
    // VALIDATION EXCEPTIONS
    // ═══════════════════════════════════════════════════════

    /**
     * Handle 400 - Validation Error
     * Triggered when request body validation fails (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, Object> error = new HashMap<>();
        Map<String, String> fieldErrors = new HashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        error.put("timestamp", LocalDateTime.now());
        error.put("status", 400);
        error.put("error", "Bad Request");
        error.put("message", "Validation failed - Please check your input");
        error.put("errors", fieldErrors);

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // ═══════════════════════════════════════════════════════
    // BUSINESS LOGIC EXCEPTIONS
    // ═══════════════════════════════════════════════════════

    /**
     * Handle 404 - Resource Not Found
     * Triggered when requested resource doesn't exist
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            ResourceNotFoundException ex) {

        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", 404);
        error.put("error", "Not Found");
        error.put("message", ex.getMessage());
        error.put("details", "The requested resource could not be found.");

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle 409 - Conflict (Duplicate or Business Rule Violation)
     * Triggered when business rules are violated (e.g., duplicate booking)
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(
            IllegalStateException ex) {

        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", 409);
        error.put("error", "Conflict");
        error.put("message", ex.getMessage());
        error.put("details", "This operation conflicts with existing data or business rules.");

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    /**
     * Handle 409 - Database Constraint Violation
     * Triggered when unique constraints are violated
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDatabaseConstraint(
            DataIntegrityViolationException ex) {

        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", 409);
        error.put("error", "Conflict");

        // Parse the error to provide user-friendly message
        String message = "Data conflict - This record already exists";
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("username")) {
                message = "Username already exists - Please choose a different username";
            } else if (ex.getMessage().contains("email")) {
                message = "Email already registered - Please use a different email";
            } else if (ex.getMessage().contains("doctor") && ex.getMessage().contains("appointment_date")) {
                message = "Doctor already has an appointment at this time - Please choose a different time slot";
            }
        }

        error.put("message", message);
        error.put("details", "Please modify your request to avoid duplicating existing data.");

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    // ═══════════════════════════════════════════════════════
    // GENERIC EXCEPTION HANDLER
    // ═══════════════════════════════════════════════════════

    /**
     * Handle 500 - Internal Server Error
     * Catches any unhandled exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(
            Exception ex) {

        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", 500);
        error.put("error", "Internal Server Error");
        error.put("message", "An unexpected error occurred");
        error.put("details", "Please contact support if this issue persists.");

        // Log the actual exception for debugging (in production, use proper logging)
        System.err.println("Unexpected error: " + ex.getMessage());
        ex.printStackTrace();

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}