package com.freightfox.dispatch.exception;


public class NoOrdersException extends RuntimeException {
    
    public NoOrdersException() {
        super("No orders available in the system for dispatch planning");
    }
    
    public NoOrdersException(String message) {
        super(message);
    }
    
    public NoOrdersException(String message, Throwable cause) {
        super(message, cause);
    }
}