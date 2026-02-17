package com.freightfox.dispatch.exception;


public class DuplicateVehicleException extends RuntimeException {
    
    private final String vehicleId;
    
    public DuplicateVehicleException(String vehicleId) {
        super(String.format("Vehicle with ID '%s' already exists in the fleet", vehicleId));
        this.vehicleId = vehicleId;
    }
    
    public String getVehicleId() {
        return vehicleId;
    }
}