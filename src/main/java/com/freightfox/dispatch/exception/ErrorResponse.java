package com.freightfox.dispatch.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {
    
    
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    
     
    private Integer status;
    
    
     
    private String error;
    

     
    private String message;
    
    
     
     
    private String path;
    
   
    private Map<String, String> validationErrors;
    
   
    private List<String> errors;
    
   
    private Object details;
    
            
    public static ErrorResponse of(Integer status, String error, String message, String path) {
        return ErrorResponse.builder()
            .status(status)
            .error(error)
            .message(message)
            .path(path)
            .build();
    }
    
    
    public static ErrorResponse validationError(String message, String path, Map<String, String> validationErrors) {
        return ErrorResponse.builder()
            .status(400)
            .error("Bad Request")
            .message(message)
            .path(path)
            .validationErrors(validationErrors)
            .build();
    }
    
   
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