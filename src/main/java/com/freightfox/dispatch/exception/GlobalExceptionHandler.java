package com.freightfox.dispatch.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

/**
 
 * 
 * @RestControllerAdvice:
 
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    
    
    /**
     
     * 
     * @param ex 
     * @param request 
     * @return 
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            WebRequest request) {
        
        log.warn("Validation failed: {} errors", ex.getBindingResult().getErrorCount());
        
        // Extract field errors into a map
        Map<String, String> validationErrors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach(error -> {
            if (error instanceof FieldError) {
                FieldError fieldError = (FieldError) error;
                String fieldName = fieldError.getField();
                String errorMessage = fieldError.getDefaultMessage();
                validationErrors.put(fieldName, errorMessage);
                
                log.debug("Validation error - Field: {}, Message: {}", fieldName, errorMessage);
            }
        });
        
        // Build error response
        ErrorResponse errorResponse = ErrorResponse.validationError(
            "Validation failed",
            extractPath(request),
            validationErrors
        );
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse);
    }
    
    
   
    @ExceptionHandler(NoOrdersException.class)
    public ResponseEntity<ErrorResponse> handleNoOrdersException(
            NoOrdersException ex,
            WebRequest request) {
        
        log.error("NoOrdersException: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            ex.getMessage(),
            extractPath(request)
        );
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse);
    }
    
   
    @ExceptionHandler(NoVehiclesException.class)
    public ResponseEntity<ErrorResponse> handleNoVehiclesException(
            NoVehiclesException ex,
            WebRequest request) {
        
        log.error("NoVehiclesException: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            ex.getMessage(),
            extractPath(request)
        );
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse);
    }
    
    
    @ExceptionHandler(InsufficientCapacityException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientCapacityException(
            InsufficientCapacityException ex,
            WebRequest request) {
        
        log.error("InsufficientCapacityException: {}", ex.getMessage());
        
        // Include capacity details in response
        Map<String, Object> details = new HashMap<>();
        if (ex.getTotalOrderWeight() != null && ex.getTotalVehicleCapacity() != null) {
            details.put("totalOrderWeight", ex.getTotalOrderWeight() + " grams");
            details.put("totalVehicleCapacity", ex.getTotalVehicleCapacity() + " grams");
            details.put("deficit", (ex.getTotalOrderWeight() - ex.getTotalVehicleCapacity()) + " grams");
        }
        
        ErrorResponse errorResponse = ErrorResponse.withDetails(
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            ex.getMessage(),
            extractPath(request),
            details
        );
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse);
    }
    
    
    @ExceptionHandler(DuplicateOrderException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateOrderException(
            DuplicateOrderException ex,
            WebRequest request) {
        
        log.error("DuplicateOrderException: {}", ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        details.put("duplicateOrderId", ex.getOrderId());
        
        ErrorResponse errorResponse = ErrorResponse.withDetails(
            HttpStatus.CONFLICT.value(),  // 409 Conflict
            "Conflict",
            ex.getMessage(),
            extractPath(request),
            details
        );
        
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(errorResponse);
    }
    
    
    @ExceptionHandler(DuplicateVehicleException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateVehicleException(
            DuplicateVehicleException ex,
            WebRequest request) {
        
        log.error("DuplicateVehicleException: {}", ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        details.put("duplicateVehicleId", ex.getVehicleId());
        
        ErrorResponse errorResponse = ErrorResponse.withDetails(
            HttpStatus.CONFLICT.value(),
            "Conflict",
            ex.getMessage(),
            extractPath(request),
            details
        );
        
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(errorResponse);
    }
    
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            WebRequest request) {
        
        log.error("IllegalArgumentException: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            ex.getMessage(),
            extractPath(request)
        );
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse);
    }
    
   
    /**
    
     * 
     * @param ex Any unhandled exception
     * @param request WebRequest
     * @return ResponseEntity with 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            WebRequest request) {
        
        // Log full stack trace for debugging
        log.error("Unhandled exception occurred", ex);
        
        // Generic message for client (don't expose internal errors)
        String clientMessage = "An unexpected error occurred. Please try again later.";
        
        // Include exception type for debugging (remove in production)
        Map<String, Object> details = new HashMap<>();
        details.put("exceptionType", ex.getClass().getSimpleName());
        // details.put("stackTrace", ex.getStackTrace());  // ⚠️ NEVER in production!
        
        ErrorResponse errorResponse = ErrorResponse.withDetails(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            clientMessage,
            extractPath(request),
            details  
        );
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(errorResponse);
    }
    
   
    /**
    
     * 
     * @param request 
     * @return 
     */
    private String extractPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}

