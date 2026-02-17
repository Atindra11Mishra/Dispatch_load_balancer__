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
public class DispatchPlanResponseDTO {

    private String message;
    private String status;  // "SUCCESS", "PARTIAL", "FAILED"
    
    // List of vehicle plans
    private List<VehiclePlanDTO> dispatchPlan;
    
    // Summary statistics
    private PlanSummaryDTO summary;
}