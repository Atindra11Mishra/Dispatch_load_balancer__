package com.freightfox.dispatch.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PlanSummaryDTO - Aggregate statistics about the plan
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanSummaryDTO {

    private Integer totalOrders;
    private Integer assignedOrders;
    private Integer unassignedOrders;
    private Integer totalVehicles;
    private Integer usedVehicles;
    private String totalDistanceCovered;  // e.g., "125.5 km"
    private Double averageUtilization;    // e.g., 78.5 (means 78.5%)
}
