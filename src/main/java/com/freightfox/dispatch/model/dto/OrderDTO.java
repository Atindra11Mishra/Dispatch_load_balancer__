package com.freightfox.dispatch.model.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 
 * @NotBlank 
 * @NotNull 
 * @Min/@Max 
 * @Pattern
 * @DecimalMin/@DecimalMax 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {

    @NotBlank(message = "Order ID is required")
   
    @Pattern(
        regexp = "^[A-Z0-9-]+$",
        message = "Order ID must contain only uppercase letters, numbers, and hyphens"
    )
    
    private String orderId;

    @NotNull(message = "Latitude is required")
    @DecimalMin(
        value = "-90.0",
        inclusive = true,
        message = "Latitude must be between -90 and 90 degrees"
    )
    @DecimalMax(
        value = "90.0",
        inclusive = true,
        message = "Latitude must be between -90 and 90 degrees"
    )

    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(
        value = "-180.0",
        inclusive = true,
        message = "Longitude must be between -180 and 180 degrees"
    )
    @DecimalMax(
        value = "180.0",
        inclusive = true,
        message = "Longitude must be between -180 and 180 degrees"
    )
    private Double longitude;

    @NotBlank(message = "Address is required")
    @Size(
        min = 10,
        max = 500,
        message = "Address must be between 10 and 500 characters"
    )
    
    private String address;

    @NotNull(message = "Package weight is required")
    @Min(value = 1, message = "Package weight must be at least 1 gram")
    @Max(value = 100000, message = "Package weight cannot exceed 100,000 grams (100 kg)")
    
    private Integer packageWeight;

    @NotBlank(message = "Priority is required")
    @Pattern(
        regexp = "^(HIGH|MEDIUM|LOW)$",
        message = "Priority must be HIGH, MEDIUM, or LOW"
    )
   
    private String priority;
}