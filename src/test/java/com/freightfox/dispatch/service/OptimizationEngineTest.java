package com.freightfox.dispatch.service;

import com.freightfox.dispatch.model.dto.DispatchPlanResponseDTO;
import com.freightfox.dispatch.model.dto.VehiclePlanDTO;
import com.freightfox.dispatch.model.entity.DeliveryOrder;
import com.freightfox.dispatch.model.entity.Vehicle;
import com.freightfox.dispatch.model.enums.Priority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for OptimizationEngine
 * 
 * TESTING STRATEGY:
 * - Test priority ordering
 * - Test capacity constraints
 * - Test distance minimization
 * - Test edge cases (no vehicles, no orders)
 * 
 * NOTE: This is a unit test without mocks because OptimizationEngine
 * has no external dependencies (it's a pure algorithm)
 */
@DisplayName("OptimizationEngine - Dispatch Algorithm Tests")
class OptimizationEngineTest {
    
    private OptimizationEngine optimizationEngine;
    
    @BeforeEach
    void setUp() {
        // GIVEN: Fresh OptimizationEngine instance for each test
        optimizationEngine = new OptimizationEngine();
    }
    
    // ========================================================================
    // PRIORITY TESTS
    // ========================================================================
    
    @Test
    @DisplayName("Should assign HIGH priority orders before MEDIUM/LOW")
    void testHighPriorityAssignedFirst() {
        // GIVEN: Orders with different priorities
        List<DeliveryOrder> orders = List.of(
            createOrder("ORD-LOW", 28.6, 77.2, 2000, Priority.LOW),
            createOrder("ORD-HIGH", 28.61, 77.21, 2000, Priority.HIGH),
            createOrder("ORD-MED", 28.62, 77.22, 2000, Priority.MEDIUM)
        );
        
        // GIVEN: One vehicle with limited capacity
        List<Vehicle> vehicles = List.of(
            createVehicle("VEH-001", 28.6, 77.2, 2500)  // Can only fit 1 order
        );
        
        // WHEN: Optimize dispatch
        DispatchPlanResponseDTO plan = optimizationEngine.optimizeDispatch(orders, vehicles);
        
        // THEN: HIGH priority order should be assigned
        assertThat(plan.getDispatchPlan()).hasSize(1);
        VehiclePlanDTO vehiclePlan = plan.getDispatchPlan().get(0);
        
        assertThat(vehiclePlan.getAssignedOrders())
            .hasSize(1)
            .first()
            .extracting("orderId", "priority")
            .containsExactly("ORD-HIGH", "HIGH");
        
        // THEN: Summary shows 1 assigned, 2 unassigned
        assertThat(plan.getSummary().getAssignedOrders()).isEqualTo(1);
        assertThat(plan.getSummary().getUnassignedOrders()).isEqualTo(2);
    }
    
    @Test
    @DisplayName("Should assign orders by priority: HIGH → MEDIUM → LOW")
    void testPriorityOrdering() {
        // GIVEN: Orders with all priority levels
        List<DeliveryOrder> orders = List.of(
            createOrder("ORD-001", 28.6, 77.2, 1000, Priority.LOW),
            createOrder("ORD-002", 28.61, 77.21, 1000, Priority.HIGH),
            createOrder("ORD-003", 28.62, 77.22, 1000, Priority.MEDIUM),
            createOrder("ORD-004", 28.63, 77.23, 1000, Priority.HIGH),
            createOrder("ORD-005", 28.64, 77.24, 1000, Priority.LOW)
        );
        
        // GIVEN: One vehicle with enough capacity for all
        List<Vehicle> vehicles = List.of(
            createVehicle("VEH-001", 28.6, 77.2, 10000)
        );
        
        // WHEN: Optimize dispatch
        DispatchPlanResponseDTO plan = optimizationEngine.optimizeDispatch(orders, vehicles);
        
        // THEN: All orders assigned to single vehicle
        assertThat(plan.getDispatchPlan()).hasSize(1);
        VehiclePlanDTO vehiclePlan = plan.getDispatchPlan().get(0);
        
        // THEN: Orders sorted by priority
        List<String> priorities = vehiclePlan.getAssignedOrders().stream()
            .map(order -> order.getPriority())
            .toList();
        
        assertThat(priorities)
            .as("Priority ordering")
            .containsExactly("HIGH", "HIGH", "MEDIUM", "LOW", "LOW");
    }
    
    // ========================================================================
    // CAPACITY CONSTRAINT TESTS
    // ========================================================================
    
    @Test
    @DisplayName("Should respect vehicle capacity constraints")
    void testCapacityConstraintRespected() {
        // GIVEN: Orders exceeding vehicle capacity
        List<DeliveryOrder> orders = List.of(
            createOrder("ORD-001", 28.6, 77.2, 8000, Priority.HIGH),
            createOrder("ORD-002", 28.61, 77.21, 5000, Priority.HIGH)
        );
        
        // GIVEN: Vehicle with 10,000g capacity (can't fit both)
        List<Vehicle> vehicles = List.of(
            createVehicle("VEH-001", 28.6, 77.2, 10000)
        );
        
        // WHEN: Optimize dispatch
        DispatchPlanResponseDTO plan = optimizationEngine.optimizeDispatch(orders, vehicles);
        
        // THEN: Only one order should be assigned
        VehiclePlanDTO vehiclePlan = plan.getDispatchPlan().get(0);
        assertThat(vehiclePlan.getAssignedOrders()).hasSize(1);
        assertThat(vehiclePlan.getTotalLoad()).isEqualTo(8000);
        
        // THEN: Vehicle should not exceed capacity
        assertThat(vehiclePlan.getTotalLoad())
            .as("Total load should not exceed capacity")
            .isLessThanOrEqualTo(10000);
        
        // THEN: One order remains unassigned
        assertThat(plan.getSummary().getUnassignedOrders()).isEqualTo(1);
    }
    
    @Test
    @DisplayName("Should distribute orders across multiple vehicles when needed")
    void testMultiVehicleDistribution() {
        // GIVEN: 3 orders, each 5kg
        List<DeliveryOrder> orders = List.of(
            createOrder("ORD-001", 28.6, 77.2, 5000, Priority.HIGH),
            createOrder("ORD-002", 28.61, 77.21, 5000, Priority.HIGH),
            createOrder("ORD-003", 28.62, 77.22, 5000, Priority.HIGH)
        );
        
        // GIVEN: 2 vehicles, each with 10kg capacity
        List<Vehicle> vehicles = List.of(
            createVehicle("VEH-001", 28.6, 77.2, 10000),
            createVehicle("VEH-002", 28.65, 77.25, 10000)
        );
        
        // WHEN: Optimize dispatch
        DispatchPlanResponseDTO plan = optimizationEngine.optimizeDispatch(orders, vehicles);
        
        // THEN: Both vehicles should be used
        assertThat(plan.getDispatchPlan())
            .as("Number of vehicles used")
            .hasSize(2);
        
        // THEN: All orders should be assigned
        assertThat(plan.getSummary().getAssignedOrders()).isEqualTo(3);
        assertThat(plan.getSummary().getUnassignedOrders()).isEqualTo(0);
        
        // THEN: Each vehicle respects capacity
        for (VehiclePlanDTO vehiclePlan : plan.getDispatchPlan()) {
            assertThat(vehiclePlan.getTotalLoad())
                .as("Vehicle %s load", vehiclePlan.getVehicleId())
                .isLessThanOrEqualTo(10000);
        }
    }
    
    // ========================================================================
    // DISTANCE MINIMIZATION TESTS
    // ========================================================================
    
    @Test
    @DisplayName("Should assign order to nearest available vehicle")
    void testNearestVehicleSelection() {
        // GIVEN: One order
        List<DeliveryOrder> orders = List.of(
            createOrder("ORD-001", 28.6, 77.2, 3000, Priority.HIGH)
        );
        
        // GIVEN: Two vehicles at different distances
        List<Vehicle> vehicles = List.of(
            createVehicle("VEH-FAR", 28.8, 77.5, 10000),   // ~40 km away
            createVehicle("VEH-NEAR", 28.61, 77.21, 10000) // ~1 km away
        );
        
        // WHEN: Optimize dispatch
        DispatchPlanResponseDTO plan = optimizationEngine.optimizeDispatch(orders, vehicles);
        
        // THEN: Order should be assigned to nearest vehicle
        assertThat(plan.getDispatchPlan()).hasSize(1);
        VehiclePlanDTO vehiclePlan = plan.getDispatchPlan().get(0);
        
        assertThat(vehiclePlan.getVehicleId())
            .as("Should select nearest vehicle")
            .isEqualTo("VEH-NEAR");
    }
    
    @Test
    @DisplayName("Should skip far vehicle if it's the only one without capacity")
    void testDistanceVsCapacityTradeoff() {
        // GIVEN: One heavy order
        List<DeliveryOrder> orders = List.of(
            createOrder("ORD-001", 28.6, 77.2, 8000, Priority.HIGH)
        );
        
        // GIVEN: Near vehicle (insufficient capacity), far vehicle (sufficient)
        List<Vehicle> vehicles = List.of(
            createVehicle("VEH-NEAR", 28.61, 77.21, 5000),  // Too small
            createVehicle("VEH-FAR", 28.8, 77.5, 10000)     // Large enough
        );
        
        // WHEN: Optimize dispatch
        DispatchPlanResponseDTO plan = optimizationEngine.optimizeDispatch(orders, vehicles);
        
        // THEN: Should assign to far vehicle (only one with capacity)
        assertThat(plan.getDispatchPlan()).hasSize(1);
        VehiclePlanDTO vehiclePlan = plan.getDispatchPlan().get(0);
        
        assertThat(vehiclePlan.getVehicleId())
            .as("Should select vehicle with capacity even if farther")
            .isEqualTo("VEH-FAR");
    }
    
    // ========================================================================
    // EDGE CASES
    // ========================================================================
    
    @Test
    @DisplayName("Should handle empty orders list")
    void testEmptyOrdersList() {
        // GIVEN: No orders
        List<DeliveryOrder> orders = new ArrayList<>();
        
        // GIVEN: Some vehicles
        List<Vehicle> vehicles = List.of(
            createVehicle("VEH-001", 28.6, 77.2, 10000)
        );
        
        // WHEN: Optimize dispatch
        DispatchPlanResponseDTO plan = optimizationEngine.optimizeDispatch(orders, vehicles);
        
        // THEN: Plan should indicate no orders
        assertThat(plan.getStatus()).isEqualTo("FAILED");
        assertThat(plan.getMessage()).contains("No orders");
        assertThat(plan.getDispatchPlan()).isEmpty();
    }
    
    @Test
    @DisplayName("Should handle empty vehicles list")
    void testEmptyVehiclesList() {
        // GIVEN: Some orders
        List<DeliveryOrder> orders = List.of(
            createOrder("ORD-001", 28.6, 77.2, 5000, Priority.HIGH)
        );
        
        // GIVEN: No vehicles
        List<Vehicle> vehicles = new ArrayList<>();
        
        // WHEN: Optimize dispatch
        DispatchPlanResponseDTO plan = optimizationEngine.optimizeDispatch(orders, vehicles);
        
        // THEN: Plan should indicate no vehicles
        assertThat(plan.getStatus()).isEqualTo("FAILED");
        assertThat(plan.getMessage()).contains("No vehicles");
        assertThat(plan.getDispatchPlan()).isEmpty();
    }
    
    @Test
    @DisplayName("Should handle when no vehicle has sufficient capacity")
    void testNoVehicleWithSufficientCapacity() {
        // GIVEN: Very heavy order
        List<DeliveryOrder> orders = List.of(
            createOrder("ORD-HEAVY", 28.6, 77.2, 50000, Priority.HIGH)
        );
        
        // GIVEN: Small vehicles
        List<Vehicle> vehicles = List.of(
            createVehicle("VEH-001", 28.6, 77.2, 10000),
            createVehicle("VEH-002", 28.61, 77.21, 10000)
        );
        
        // WHEN: Optimize dispatch
        DispatchPlanResponseDTO plan = optimizationEngine.optimizeDispatch(orders, vehicles);
        
        // THEN: Order should remain unassigned
        assertThat(plan.getSummary().getAssignedOrders()).isEqualTo(0);
        assertThat(plan.getSummary().getUnassignedOrders()).isEqualTo(1);
        assertThat(plan.getStatus()).isEqualTo("PARTIAL");
    }
    
    @Test
    @DisplayName("Should calculate utilization percentage correctly")
    void testUtilizationCalculation() {
        // GIVEN: Orders totaling 8kg
        List<DeliveryOrder> orders = List.of(
            createOrder("ORD-001", 28.6, 77.2, 5000, Priority.HIGH),
            createOrder("ORD-002", 28.61, 77.21, 3000, Priority.HIGH)
        );
        
        // GIVEN: 10kg capacity vehicle
        List<Vehicle> vehicles = List.of(
            createVehicle("VEH-001", 28.6, 77.2, 10000)
        );
        
        // WHEN: Optimize dispatch
        DispatchPlanResponseDTO plan = optimizationEngine.optimizeDispatch(orders, vehicles);
        
        // THEN: Utilization should be 80%
        VehiclePlanDTO vehiclePlan = plan.getDispatchPlan().get(0);
        assertThat(vehiclePlan.getUtilizationPercentage())
            .as("Utilization percentage")
            .isEqualTo(80.0);
    }
    
    @Test
    @DisplayName("Should track total distance covered")
    void testTotalDistanceTracking() {
        // GIVEN: Multiple orders
        List<DeliveryOrder> orders = List.of(
            createOrder("ORD-001", 28.6, 77.2, 2000, Priority.HIGH),
            createOrder("ORD-002", 28.7, 77.3, 2000, Priority.HIGH)
        );
        
        // GIVEN: One vehicle
        List<Vehicle> vehicles = List.of(
            createVehicle("VEH-001", 28.6, 77.2, 10000)
        );
        
        // WHEN: Optimize dispatch
        DispatchPlanResponseDTO plan = optimizationEngine.optimizeDispatch(orders, vehicles);
        
        // THEN: Total distance should be sum of individual distances
        VehiclePlanDTO vehiclePlan = plan.getDispatchPlan().get(0);
        assertThat(vehiclePlan.getTotalDistance())
            .as("Total distance string")
            .matches("\\d+\\.\\d{2} km");
        
        // Extract numeric value and verify it's reasonable
        String distanceStr = vehiclePlan.getTotalDistance().replace(" km", "");
        double distance = Double.parseDouble(distanceStr);
        assertThat(distance).isGreaterThan(0);
    }
    
    // ========================================================================
    // HELPER METHODS (Test Data Builders)
    // ========================================================================
    
    /**
     * Create test DeliveryOrder
     */
    private DeliveryOrder createOrder(String orderId, double lat, double lon, 
                                      int weight, Priority priority) {
        return DeliveryOrder.builder()
            .orderId(orderId)
            .latitude(lat)
            .longitude(lon)
            .address("Test Address for " + orderId)
            .packageWeight(weight)
            .priority(priority)
            .build();
    }
    
    /**
     * Create test Vehicle
     */
    private Vehicle createVehicle(String vehicleId, double lat, double lon, int capacity) {
        return Vehicle.builder()
            .vehicleId(vehicleId)
            .currentLatitude(lat)
            .currentLongitude(lon)
            .currentAddress("Test Location for " + vehicleId)
            .capacity(capacity)
            .build();
    }
}