package com.mhoms.mhomsservices.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom handler for 403 Access Denied errors
 * Provides user-friendly JSON responses instead of default HTML error page
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException)
            throws IOException, ServletException {

        // Set response status and content type
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Determine which endpoint was accessed
        String requestUri = request.getRequestURI();
        String method = request.getMethod();

        // Create custom error message based on endpoint
        String customMessage = getCustomMessage(method, requestUri);

        // Build error response
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now().toString());
        errorDetails.put("status", 403);
        errorDetails.put("error", "Forbidden");
        errorDetails.put("message", customMessage);
        errorDetails.put("path", requestUri);
        errorDetails.put("details", "Your current role does not have permission to access this resource. Contact your administrator if you believe this is an error.");

        // Write JSON response
        response.getWriter().write(objectMapper.writeValueAsString(errorDetails));
    }

    /**
     * Generate custom message based on endpoint and HTTP method
     */
    private String getCustomMessage(String method, String uri) {

        // Patient endpoints
        if (uri.contains("/patients")) {
            if (method.equals("POST")) {
                return "Access Denied - Only ADMIN can create patients";
            } else if (method.equals("GET")) {
                return "Access Denied - Only ADMIN and DOCTOR can view patients";
            } else if (method.equals("PUT") || method.equals("DELETE")) {
                return "Access Denied - Only ADMIN can modify patient records";
            }
        }

        // Doctor endpoints
        if (uri.contains("/doctors")) {
            if (method.equals("POST")) {
                return "Access Denied - Only ADMIN can register doctors";
            } else if (method.equals("PUT") || method.equals("DELETE")) {
                return "Access Denied - Only ADMIN can modify doctor profiles";
            }
        }

        // Appointment endpoints
        if (uri.contains("/appointments")) {
            if (method.equals("POST")) {
                return "Access Denied - Only ADMIN and PATIENT can book appointments";
            } else if (uri.contains("/status") && method.equals("PUT")) {
                return "Access Denied - Only ADMIN and DOCTOR can update appointment status";
            } else if (method.equals("DELETE")) {
                return "Access Denied - Only ADMIN can delete appointments";
            }
        }

        // Default message
        return "Access Denied - You don't have permission to perform this action";
    }
}