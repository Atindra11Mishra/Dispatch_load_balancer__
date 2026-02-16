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
 * VehicleRequestDTO - Wrapper for batch vehicle submissions
 * 
 * POST /api/dispatch/vehicles
 * {
 *   "vehicles": [
 *     { "vehicleId": "VEH-001", ... },
 *     { "vehicleId": "VEH-002", ... }
 *   ]
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleRequestDTO {

    @NotEmpty(message = "Vehicles list cannot be empty")
    @Size(
        min = 1,
        max = 50,
        message = "Must submit between 1 and 50 vehicles at once"
    )
    // Smaller batch size than orders - fleet is usually smaller
    
    @Valid
    // Validates each VehicleDTO in the list
    private List<VehicleDTO> vehicles;
}