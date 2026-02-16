package com.freightfox.dispatch.model.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * VehicleDTO - Individual Delivery Vehicle
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleDTO {

    @NotBlank(message = "Vehicle ID is required")
    @Pattern(
        regexp = "^[A-Z0-9-]+$",
        message = "Vehicle ID must contain only uppercase letters, numbers, and hyphens"
    )
    // Format examples: "VEH-001", "TRUCK-DL-1234"
    private String vehicleId;

    @NotNull(message = "Capacity is required")
    @Min(value = 1000, message = "Capacity must be at least 1000 grams (1 kg)")
    @Max(value = 50000000, message = "Capacity cannot exceed 50,000 kg")
    // Realistic vehicle capacity range
    // Min: Small bike courier (1 kg)
    // Max: Large truck (50,000 kg)
    private Integer capacity;

    @NotNull(message = "Current latitude is required")
    @DecimalMin(
        value = "-90.0",
        inclusive = true,
        message = "Latitude must be between -90 and 90 degrees"
    )
    @DecimalMax(
        value = "90.0",
        inclusive = true,
        message = "Latitude must be between -90 and 90 degrees"
    )
    private Double currentLatitude;

    @NotNull(message = "Current longitude is required")
    @DecimalMin(
        value = "-180.0",
        inclusive = true,
        message = "Longitude must be between -180 and 180 degrees"
    )
    @DecimalMax(
        value = "180.0",
        inclusive = true,
        message = "Longitude must be between -180 and 180 degrees"
    )
    private Double currentLongitude;

    @Size(
        min = 10,
        max = 500,
        message = "Current address must be between 10 and 500 characters"
    )
    // Made optional with @Size instead of @NotBlank
    // Some vehicles might not have address, only coordinates
    private String currentAddress;
}