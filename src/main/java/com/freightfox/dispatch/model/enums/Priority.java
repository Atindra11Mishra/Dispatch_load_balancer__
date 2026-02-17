package com.freightfox.dispatch.model.enums;
public enum Priority {
    HIGH,      
    MEDIUM,    
    LOW;       
    
    public int getSortOrder() {
        return switch (this) {
            case HIGH -> 3;
            case MEDIUM -> 2;
            case LOW -> 1;
        };
    }
}