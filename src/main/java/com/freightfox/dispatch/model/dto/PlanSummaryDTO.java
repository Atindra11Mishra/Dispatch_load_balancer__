package com.freightfox.dispatch.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


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
    private String totalDistanceCovered;  
    private Double averageUtilization;    
}
