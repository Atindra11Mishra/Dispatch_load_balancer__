package com.freightfox.dispatch.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DispatchPlanResponseDTO - Complete dispatch optimization result
 * 
 * GET /api/dispatch/plan
 * {
 *   "message": "Dispatch plan generated successfully",
 *   "status": "SUCCESS",
 *   "dispatchPlan": [
 *     { "vehicleId": "VEH-001", "totalLoad": 15000, ... },
 *     { "vehicleId": "VEH-002", "totalLoad": 12000, ... }
 *   ],
 *   "summary": { ... }
 * }
 */
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