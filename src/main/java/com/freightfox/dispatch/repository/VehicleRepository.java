package com.freightfox.dispatch.repository;

import com.freightfox.dispatch.model.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;



@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, String> {
    
   
    
    List<Vehicle> findByCapacityGreaterThanEqual(Integer minCapacity);
    
    
    List<Vehicle> findAllByOrderByCapacityDesc();
    
   
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
    
   
    boolean existsByVehicleId(String vehicleId);
   
    long countByCapacityGreaterThan(Integer capacity);
}