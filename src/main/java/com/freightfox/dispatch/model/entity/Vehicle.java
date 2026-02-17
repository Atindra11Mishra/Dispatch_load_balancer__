package com.freightfox.dispatch.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;


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
     
     * @param weight 
     * @return 
     */
    public boolean canAccommodate(Integer weight) {
        return this.capacity >= weight;
    }

    /**
     
     * @param currentLoad 
     * @return 
     */
    public Integer getRemainingCapacity(Integer currentLoad) {
        return this.capacity - currentLoad;
    }
}