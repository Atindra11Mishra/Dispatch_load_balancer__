package com.freightfox.dispatch.model.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OrderDTO - Individual Delivery Order
 * 
 * VALIDATION ANNOTATIONS:
 * @NotBlank - String must not be null, empty, or whitespace
 * @NotNull - Field must not be null (but can be empty for strings)
 * @Min/@Max - Numeric range validation
 * @Pattern - Regex validation
 * @DecimalMin/@DecimalMax - Decimal range with inclusive/exclusive options
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {

    @NotBlank(message = "Order ID is required")
    // Validates: orderId != null && !orderId.trim().isEmpty()
    // Like: if (!orderId?.trim()) throw error
    @Pattern(
        regexp = "^[A-Z0-9-]+$",
        message = "Order ID must contain only uppercase letters, numbers, and hyphens"
    )
    // Regex validation - ensures format like "ORD-001", "ORDER-2024-001"
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
    // Geographic coordinate validation
    // Inclusive = true means -90.0 and 90.0 are valid
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
    // String length validation
    // Like: if (address.length < 10 || address.length > 500) throw error
    private String address;

    @NotNull(message = "Package weight is required")
    @Min(value = 1, message = "Package weight must be at least 1 gram")
    @Max(value = 100000, message = "Package weight cannot exceed 100,000 grams (100 kg)")
    // Prevents negative or zero weights
    // Max 100kg for single package (adjust based on business rules)
    private Integer packageWeight;

    @NotBlank(message = "Priority is required")
    @Pattern(
        regexp = "^(HIGH|MEDIUM|LOW)$",
        message = "Priority must be HIGH, MEDIUM, or LOW"
    )
    // Enum-like string validation
    // Ensures only these 3 values are accepted
    // Alternative: Use @ValidEnum custom validator (we'll create if needed)
    private String priority;
}