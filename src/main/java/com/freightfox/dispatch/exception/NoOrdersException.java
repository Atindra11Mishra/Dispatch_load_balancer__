package com.freightfox.dispatch.exception;

/**
 * NoOrdersException - Thrown when no orders are available for dispatch
 * 
 * Custom exceptions provide:
 * 1. Type-safe error handling
 * 2. Meaningful error messages
 * 3. Specific catch blocks
 * 
 * RuntimeException = unchecked exception (no need for try-catch)
 */
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