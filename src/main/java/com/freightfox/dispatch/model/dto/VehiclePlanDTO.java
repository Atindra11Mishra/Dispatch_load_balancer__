package com.freightfox.dispatch.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehiclePlanDTO {

    private String vehicleId;
    
    
    private Integer totalLoad;
    
    
    private String totalDistance;  
    
    
    private List<AssignedOrderDTO> assignedOrders;
    
    
    private Double utilizationPercentage;  
    
    
    private Integer orderCount;
}