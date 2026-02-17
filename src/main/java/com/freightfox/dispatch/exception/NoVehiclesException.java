package com.freightfox.dispatch.exception;


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