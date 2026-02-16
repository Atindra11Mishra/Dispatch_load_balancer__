package com.freightfox.dispatch.exception;

/**
 * NoVehiclesException - Thrown when no vehicles are available for dispatch
 */
public class NoVehiclesException extends RuntimeException {
    
    public NoVehiclesException() {
        super("No vehicles available in the fleet for dispatch planning");
    }
    
    public NoVehiclesException(String message) {
        super(message);
    }
    
    public NoVehiclesException(String message, Throwable cause) {
        super(message, cause);
    }
}