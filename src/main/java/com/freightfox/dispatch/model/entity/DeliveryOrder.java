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

/**
 * DeliveryOrder Entity - Represents delivery_orders table in PostgreSQL
 * 
 * ANNOTATION GUIDE (from top to bottom):
 */

// === LOMBOK ANNOTATIONS (Code Generation) ===
@Data
// Generates: getters, setters, toString(), equals(), hashCode()
// Like: Object.defineProperty() for all fields automatically
// Without @Data, you'd write ~100 lines of boilerplate code!

@NoArgsConstructor
// Generates: public DeliveryOrder() {}
// Required by JPA to create objects via reflection

@AllArgsConstructor
// Generates: constructor with ALL fields as parameters
// Like: constructor(orderId, latitude, longitude, ...) {}

@Builder
// Enables builder pattern:
// DeliveryOrder.builder().orderId("123").latitude(28.5).build()
// Like spreading objects in JS but type-safe

// === JPA ANNOTATIONS (Database Mapping) ===
@Entity
// Marks this class as a JPA entity (database table)
// Like Mongoose model: mongoose.model('DeliveryOrder', schema)

@Table(name = "delivery_orders")
// Specifies exact table name in database
// Without this, JPA uses class name "delivery_order" (snake_case conversion)

public class DeliveryOrder {

    // === PRIMARY KEY ===
    @Id
    // Marks this field as primary key (like _id in MongoDB)
    
    @Column(name = "order_id", nullable = false, unique = true, length = 50)
    // Maps to database column with constraints:
    // - name: exact column name
    // - nullable = false: NOT NULL constraint
    // - unique = true: UNIQUE constraint
    // - length = 50: VARCHAR(50)
    private String orderId;

    // === LOCATION FIELDS ===
    @Column(name = "latitude", nullable = false)
    // DECIMAL type in PostgreSQL (default precision)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "address", length = 500)
    // VARCHAR(500) - address can be long
    private String address;

    // === PACKAGE DETAILS ===
    @Column(name = "package_weight", nullable = false)
    // INTEGER type in PostgreSQL
    // Represents weight in grams or kg (document in your API)
    private Integer packageWeight;

    // === PRIORITY (ENUM MAPPING) ===
    @Enumerated(EnumType.STRING)
    // Stores enum as STRING in database: 'HIGH', 'MEDIUM', 'LOW'
    // Alternative: EnumType.ORDINAL stores as 0, 1, 2 (avoid - fragile)
    
    @Column(name = "priority", nullable = false)
    private Priority priority;

    // === AUDIT FIELDS (Automatic Timestamps) ===
    @CreationTimestamp
    // Automatically sets this field when entity is first saved
    // Like Mongoose: { timestamps: true } → createdAt
    
    @Column(name = "created_at", nullable = false, updatable = false)
    // updatable = false: prevents changing this value on UPDATE queries
    private LocalDateTime createdAt;

    @UpdateTimestamp
    // Automatically updates this field on every save/update
    // Like Mongoose: { timestamps: true } → updatedAt
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // === ALTERNATIVE TIMESTAMP APPROACH (Manual) ===
    // If you don't use @CreationTimestamp/@UpdateTimestamp:
    
    @PrePersist
    // Lifecycle hook - called BEFORE entity is saved for first time
    // Like Mongoose pre('save') hook
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    // Lifecycle hook - called BEFORE entity is updated
    // Like Mongoose pre('save') when doc already exists
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}