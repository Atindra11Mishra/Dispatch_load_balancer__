package com.freightfox.dispatch.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * VehiclePlanDTO - Individual vehicle's dispatch plan
 * 
 * Shows what orders are assigned to this vehicle
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehiclePlanDTO {

    private String vehicleId;
    
    // Total weight of all assigned orders
    private Integer totalLoad;
    
    // Total distance for all deliveries (formatted string)
    private String totalDistance;  // e.g., "15.8 km"
    
    // List of orders assigned to this vehicle
    // Sorted by priority (HIGH → MEDIUM → LOW)
    private List<AssignedOrderDTO> assignedOrders;
    
    // Utilization percentage (optional but useful)
    private Double utilizationPercentage;  // e.g., 85.5 (means 85.5%)
    
    // Number of orders assigned
    private Integer orderCount;
}