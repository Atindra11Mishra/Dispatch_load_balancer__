package com.freightfox.dispatch.repository;

import com.freightfox.dispatch.model.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * VehicleRepository - Database access for Vehicle entity
 * 
 * EXTENDS JpaRepository<Vehicle, String>
 * - Vehicle: The entity class
 * - String: Primary key type (vehicleId is String)
 */

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, String> {
    
    // =====================================================================
    // AUTO-PROVIDED METHODS (FREE from JpaRepository)
    // =====================================================================
    
    // Vehicle save(Vehicle vehicle)
    // Optional<Vehicle> findById(String vehicleId)
    // List<Vehicle> findAll()
    // void deleteById(String vehicleId)
    // long count()
    // boolean existsById(String vehicleId)
    // List<Vehicle> saveAll(List<Vehicle> vehicles)
    
    
    // =====================================================================
    // CUSTOM QUERY METHODS (For future enhancements)
    // =====================================================================
    
    /**
     * Find vehicles with capacity greater than or equal to a threshold
     * 
     * Useful for: Finding vehicles that can handle heavy orders
     * 
     * Generated SQL:
     * SELECT * FROM vehicles WHERE capacity >= ?
     */
    List<Vehicle> findByCapacityGreaterThanEqual(Integer minCapacity);
    
    /**
     * Find vehicles sorted by capacity (largest first)
     * 
     * Useful for: Prioritizing high-capacity vehicles
     * 
     * Pattern: findAllBy + OrderBy + Field + Direction
     */
    List<Vehicle> findAllByOrderByCapacityDesc();
    
    /**
     * Custom query: Find vehicles within a bounding box
     * (Useful for location-based filtering in future)
     * 
     * Finds vehicles in a geographic area defined by:
     * - minLat, maxLat (latitude bounds)
     * - minLng, maxLng (longitude bounds)
     * 
     * Native SQL for geographic queries
     */
    @Query(
        value = "SELECT * FROM vehicles " +
                "WHERE current_latitude BETWEEN :minLat AND :maxLat " +
                "AND current_longitude BETWEEN :minLng AND :maxLng",
        nativeQuery = true
    )
    List<Vehicle> findVehiclesInBoundingBox(
        @Param("minLat") Double minLat,
        @Param("maxLat") Double maxLat,
        @Param("minLng") Double minLng,
        @Param("maxLng") Double maxLng
    );
    
    /**
     * Check if vehicle exists by ID
     * (Alternative to existsById for custom naming)
     */
    boolean existsByVehicleId(String vehicleId);
    
    /**
     * Count vehicles with capacity above threshold
     */
    long countByCapacityGreaterThan(Integer capacity);
}