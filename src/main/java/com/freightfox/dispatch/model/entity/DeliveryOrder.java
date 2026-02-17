package com.freightfox.dispatch.model.entity;

import com.freightfox.dispatch.model.enums.Priority;
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

@Table(name = "delivery_orders")

public class DeliveryOrder {

    
    @Id
    
    @Column(name = "order_id", nullable = false, unique = true, length = 50)
    
    private String orderId;

    
    @Column(name = "latitude", nullable = false)
    
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "address", length = 500)
    
    private String address;

    
    @Column(name = "package_weight", nullable = false)
    
    
    private Integer packageWeight;

    
    @Enumerated(EnumType.STRING)
    
    // Alternative: EnumType.ORDINAL stores as 0, 1, 2 (avoid - fragile)
    
    @Column(name = "priority", nullable = false)
    private Priority priority;

    
    @CreationTimestamp
    
    
    @Column(name = "created_at", nullable = false, updatable = false)
    
    private LocalDateTime createdAt;

    @UpdateTimestamp
    
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    
    
    @PrePersist
    
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}