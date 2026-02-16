package com.freightfox.dispatch.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * OrderRequestDTO - Wrapper for batch order submissions
 * 
 * POST /api/dispatch/orders
 * {
 *   "orders": [
 *     { "orderId": "ORD-001", ... },
 *     { "orderId": "ORD-002", ... }
 *   ]
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequestDTO {

    @NotEmpty(message = "Orders list cannot be empty")
    // Validates: orders != null && !orders.isEmpty()
    // More specific than @NotNull for collections
    
    @Size(
        min = 1,
        max = 100,
        message = "Must submit between 1 and 100 orders at once"
    )
    // Batch size limits - prevents memory issues
    // Adjust max based on your server capacity
    
    @Valid
    // CRITICAL: Triggers validation on EACH OrderDTO in the list
    // Without @Valid, nested objects aren't validated
    // Like: orders.forEach(order => validate(order))
    private List<OrderDTO> orders;
}