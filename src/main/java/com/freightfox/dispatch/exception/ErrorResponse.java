package com.freightfox.dispatch.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * ErrorResponse - Standardized error response format
 * 
 * Provides consistent error structure across all API endpoints
 * 
 * EXAMPLE RESPONSES:
 * 
 * 1. Validation Error:
 * {
 *   "timestamp": "2024-02-15T14:30:00",
 *   "status": 400,
 *   "error": "Bad Request",
 *   "message": "Validation failed",
 *   "path": "/api/dispatch/orders",
 *   "validationErrors": {
 *     "orderId": "Order ID is required",
 *     "latitude": "Latitude must be between -90 and 90 degrees"
 *   }
 * }
 * 
 * 2. Business Logic Error:
 * {
 *   "timestamp": "2024-02-15T14:30:00",
 *   "status": 400,
 *   "error": "Bad Request",
 *   "message": "No orders available in the system for dispatch planning",
 *   "path": "/api/dispatch/plan"
 * }
 * 
 * 3. Server Error:
 * {
 *   "timestamp": "2024-02-15T14:30:00",
 *   "status": 500,
 *   "error": "Internal Server Error",
 *   "message": "An unexpected error occurred",
 *   "path": "/api/dispatch/plan"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {
    
    /**
     * Timestamp when error occurred
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    /**
     * HTTP status code (400, 404, 500, etc.)
     */
    private Integer status;
    
    /**
     * HTTP status text ("Bad Request", "Not Found", etc.)
     */
    private String error;
    
    /**
     * Human-readable error message
     */
    private String message;
    
    /**
     * Request path where error occurred
     */
    private String path;
    
    /**
     * Validation errors (field-level)
     * Only present for validation failures
     * 
     * Format: { "fieldName": "error message" }
     */
    private Map<String, String> validationErrors;
    
    /**
     * List of error messages (alternative format)
     * Used when multiple errors don't map to specific fields
     */
    private List<String> errors;
    
    /**
     * Additional details (optional, for debugging)
     * Should NOT expose sensitive info in production
     */
    private Object details;
    
    // ========================================================================
    // FACTORY METHODS (Convenience constructors)
    // ========================================================================
    
    /**
     * Create simple error response
     */
    public static ErrorResponse of(Integer status, String error, String message, String path) {
        return ErrorResponse.builder()
            .status(status)
            .error(error)
            .message(message)
            .path(path)
            .build();
    }
    
    /**
     * Create validation error response
     */
    public static ErrorResponse validationError(String message, String path, Map<String, String> validationErrors) {
        return ErrorResponse.builder()
            .status(400)
            .error("Bad Request")
            .message(message)
            .path(path)
            .validationErrors(validationErrors)
            .build();
    }
    
    /**
     * Create error response with details
     */
    public static ErrorResponse withDetails(Integer status, String error, String message, String path, Object details) {
        return ErrorResponse.builder()
            .status(status)
            .error(error)
            .message(message)
            .path(path)
            .details(details)
            .build();
    }
}