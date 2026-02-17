package com.freightfox.dispatch.exception;


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