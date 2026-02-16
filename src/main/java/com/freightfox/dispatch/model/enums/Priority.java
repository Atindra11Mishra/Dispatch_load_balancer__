package com.freightfox.dispatch.model.enums;

/**
 * Priority Enum for Delivery Orders
 * 
 * In Java, enums are type-safe constants (better than const objects in JS)
 * Think of this like: const Priority = { HIGH: 'HIGH', MEDIUM: 'MEDIUM', LOW: 'LOW' }
 * But with compile-time safety - you can't accidentally use 'HGIH' (typo)
 */
public enum Priority {
    HIGH,      // Highest priority - processed first
    MEDIUM,    // Normal priority
    LOW;       // Lowest priority - processed last
    
    /**
     * Custom method to get numeric value for sorting
     * HIGH = 3, MEDIUM = 2, LOW = 1
     */
    public int getSortOrder() {
        return switch (this) {
            case HIGH -> 3;
            case MEDIUM -> 2;
            case LOW -> 1;
        };
    }
}