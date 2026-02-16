package com.freightfox.dispatch.exception;

/**
 * InsufficientCapacityException - Thrown when total vehicle capacity 
 * cannot accommodate all orders
 */
public class InsufficientCapacityException extends RuntimeException {
    
    private final Integer totalOrderWeight;
    private final Integer totalVehicleCapacity;
    
    public InsufficientCapacityException(Integer totalOrderWeight, Integer totalVehicleCapacity) {
        super(String.format(
            "Insufficient total capacity: Orders require %d grams but fleet capacity is only %d grams",
            totalOrderWeight,
            totalVehicleCapacity
        ));
        this.totalOrderWeight = totalOrderWeight;
        this.totalVehicleCapacity = totalVehicleCapacity;
    }
    
    public InsufficientCapacityException(String message) {
        super(message);
        this.totalOrderWeight = null;
        this.totalVehicleCapacity = null;
    }
    
    public Integer getTotalOrderWeight() {
        return totalOrderWeight;
    }
    
    public Integer getTotalVehicleCapacity() {
        return totalVehicleCapacity;
    }
}