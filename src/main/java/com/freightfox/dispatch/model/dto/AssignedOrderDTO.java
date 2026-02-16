package com.freightfox.dispatch.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AssignedOrderDTO - Order details in dispatch plan response
 * 
 * Simplified version of OrderDTO for response purposes
 * Doesn't need validation annotations (outgoing data)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignedOrderDTO {

    private String orderId;
    private String address;
    private Integer packageWeight;
    private String priority;
    
    // Distance from vehicle's starting point to this order
    private String distanceFromVehicle;  // e.g., "5.2 km"
}