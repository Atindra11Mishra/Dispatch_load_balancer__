package com.freightfox.dispatch.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Vehicle Entity - Represents vehicles table in PostgreSQL
 * 
 * Stores delivery vehicle fleet information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @Column(name = "vehicle_id", nullable = false, unique = true, length = 50)
    private String vehicleId;

    @Column(name = "capacity", nullable = false)
    // Maximum weight capacity in same unit as packageWeight
    // Add validation: must be > 0
    private Integer capacity;

    // === CURRENT LOCATION ===
    @Column(name = "current_latitude", nullable = false)
    private Double currentLatitude;

    @Column(name = "current_longitude", nullable = false)
    private Double currentLongitude;

    @Column(name = "current_address", length = 500)
    private String currentAddress;

    // === AUDIT TIMESTAMPS ===
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // === LIFECYCLE HOOKS (Alternative to @CreationTimestamp) ===
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // === CUSTOM BUSINESS METHODS ===
    
    /**
     * Check if vehicle can accommodate a specific weight
     * @param weight Weight to check against capacity
     * @return true if vehicle has sufficient capacity
     */
    public boolean canAccommodate(Integer weight) {
        return this.capacity >= weight;
    }

    /**
     * Get available capacity after accounting for current load
     * (You'll track currentLoad in service layer)
     * @param currentLoad Current weight already assigned
     * @return Remaining capacity
     */
    public Integer getRemainingCapacity(Integer currentLoad) {
        return this.capacity - currentLoad;
    }
}