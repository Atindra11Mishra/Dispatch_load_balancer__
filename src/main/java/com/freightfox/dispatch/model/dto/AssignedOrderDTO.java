package com.freightfox.dispatch.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignedOrderDTO {

    private String orderId;
    private String address;
    private Integer packageWeight;
    private String priority;
    
    
    private String distanceFromVehicle;  
}