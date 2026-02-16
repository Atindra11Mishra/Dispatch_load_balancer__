package com.freightfox.dispatch.exception;

/**
 * DuplicateOrderException - Thrown when attempting to save an order 
 * with an ID that already exists
 */
public class DuplicateOrderException extends RuntimeException {
    
    private final String orderId;
    
    public DuplicateOrderException(String orderId) {
        super(String.format("Order with ID '%s' already exists in the system", orderId));
        this.orderId = orderId;
    }
    
    public String getOrderId() {
        return orderId;
    }
}