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


@DisplayName("OptimizationEngine - Dispatch Algorithm Tests")
class OptimizationEngineTest {
    
    private OptimizationEngine optimizationEngine;
    
    @BeforeEach
    void setUp() {
        
        optimizationEngine = new OptimizationEngine();
    }
    
    
    
    @Test
    @DisplayName("Should assign HIGH priority orders before MEDIUM/LOW")
    void testHighPriorityAssignedFirst() {
        
        List<DeliveryOrder> orders = List.of(
            createOrder("ORD-LOW", 28.6, 77.2, 2000, Priority.LOW),
            createOrder("ORD-HIGH", 28.61, 77.21, 2000, Priority.HIGH),
            createOrder("ORD-MED", 28.62, 77.22, 2000, Priority.MEDIUM)
        );
        
        
        List<Vehicle> vehicles = List.of(
            createVehicle("VEH-001", 28.6, 77.2, 2500)  // Can only fit 1 order
        );
        
       
        DispatchPlanResponseDTO plan = optimizationEngine.optimizeDispatch(orders, vehicles);
        
        
        assertThat(plan.getDispatchPlan()).hasSize(1);
        VehiclePlanDTO vehiclePlan = plan.getDispatchPlan().get(0);
        
        assertThat(vehiclePlan.getAssignedOrders())
            .hasSize(1)
            .first()
            .extracting("orderId", "priority")
            .containsExactly("ORD-HIGH", "HIGH");
        
         
        assertThat(plan.getSummary().getAssignedOrders()).isEqualTo(1);
        assertThat(plan.getSummary().getUnassignedOrders()).isEqualTo(2);
    }
    
    @Test
    @DisplayName("Should assign orders by priority: HIGH → MEDIUM → LOW")
    void testPriorityOrdering() {
        
        List<DeliveryOrder> orders = List.of(
            createOrder("ORD-001", 28.6, 77.2, 1000, Priority.LOW),
            createOrder("ORD-002", 28.61, 77.21, 1000, Priority.HIGH),
            createOrder("ORD-003", 28.62, 77.22, 1000, Priority.MEDIUM),
            createOrder("ORD-004", 28.63, 77.23, 1000, Priority.HIGH),
            createOrder("ORD-005", 28.64, 77.24, 1000, Priority.LOW)
        );
        
       
        List<Vehicle> vehicles = List.of(
            createVehicle("VEH-001", 28.6, 77.2, 10000)
        );
        
       
        DispatchPlanResponseDTO plan = optimizationEngine.optimizeDispatch(orders, vehicles);
        
        
        assertThat(plan.getDispatchPlan()).hasSize(1);
        VehiclePlanDTO vehiclePlan = plan.getDispatchPlan().get(0);
        
        
        List<String> priorities = vehiclePlan.getAssignedOrders().stream()
            .map(order -> order.getPriority())
            .toList();
        
        assertThat(priorities)
            .as("Priority ordering")
            .containsExactly("HIGH", "HIGH", "MEDIUM", "LOW", "LOW");
    }
    
    
    @Test
    @DisplayName("Should respect vehicle capacity constraints")
    void testCapacityConstraintRespected() {
        
        List<DeliveryOrder> orders = List.of(
            createOrder("ORD-001", 28.6, 77.2, 8000, Priority.HIGH),
            createOrder("ORD-002", 28.61, 77.21, 5000, Priority.HIGH)
        );
        
        
        List<Vehicle> vehicles = List.of(
            createVehicle("VEH-001", 28.6, 77.2, 10000)
        );
        
       
        DispatchPlanResponseDTO plan = optimizationEngine.optimizeDispatch(orders, vehicles);
        
        
        VehiclePlanDTO vehiclePlan = plan.getDispatchPlan().get(0);
        assertThat(vehiclePlan.getAssignedOrders()).hasSize(1);
        assertThat(vehiclePlan.getTotalLoad()).isEqualTo(8000);
        
        
        assertThat(vehiclePlan.getTotalLoad())
            .as("Total load should not exceed capacity")
            .isLessThanOrEqualTo(10000);
        
        
        assertThat(plan.getSummary().getUnassignedOrders()).isEqualTo(1);
    }
    
    @Test
    @DisplayName("Should distribute orders across multiple vehicles when needed")
    void testMultiVehicleDistribution() {
        
        List<DeliveryOrder> orders = List.of(
            createOrder("ORD-001", 28.6, 77.2, 5000, Priority.HIGH),
            createOrder("ORD-002", 28.61, 77.21, 5000, Priority.HIGH),
            createOrder("ORD-003", 28.62, 77.22, 5000, Priority.HIGH)
        );
        
        
        List<Vehicle> vehicles = List.of(
            createVehicle("VEH-001", 28.6, 77.2, 10000),
            createVehicle("VEH-002", 28.65, 77.25, 10000)
        );
        
        
        DispatchPlanResponseDTO plan = optimizationEngine.optimizeDispatch(orders, vehicles);
        
        
        assertThat(plan.getDispatchPlan())
            .as("Number of vehicles used")
            .hasSize(2);
        
        
        assertThat(plan.getSummary().getAssignedOrders()).isEqualTo(3);
        assertThat(plan.getSummary().getUnassignedOrders()).isEqualTo(0);
        
        
        for (VehiclePlanDTO vehiclePlan : plan.getDispatchPlan()) {
            assertThat(vehiclePlan.getTotalLoad())
                .as("Vehicle %s load", vehiclePlan.getVehicleId())
                .isLessThanOrEqualTo(10000);
        }
    }
    
   
    @Test
    @DisplayName("Should assign order to nearest available vehicle")
    void testNearestVehicleSelection() {
        
        List<DeliveryOrder> orders = List.of(
            createOrder("ORD-001", 28.6, 77.2, 3000, Priority.HIGH)
        );
        
        
        List<Vehicle> vehicles = List.of(
            createVehicle("VEH-FAR", 28.8, 77.5, 10000),   
            createVehicle("VEH-NEAR", 28.61, 77.21, 10000) 
        );
        
        
        DispatchPlanResponseDTO plan = optimizationEngine.optimizeDispatch(orders, vehicles);
        
        
        assertThat(plan.getDispatchPlan()).hasSize(1);
        VehiclePlanDTO vehiclePlan = plan.getDispatchPlan().get(0);
        
        assertThat(vehiclePlan.getVehicleId())
            .as("Should select nearest vehicle")
            .isEqualTo("VEH-NEAR");
    }
    
    @Test
    @DisplayName("Should skip far vehicle if it's the only one without capacity")
    void testDistanceVsCapacityTradeoff() {
        
        List<DeliveryOrder> orders = List.of(
            createOrder("ORD-001", 28.6, 77.2, 8000, Priority.HIGH)
        );
        
        
        List<Vehicle> vehicles = List.of(
            createVehicle("VEH-NEAR", 28.61, 77.21, 5000),  // Too small
            createVehicle("VEH-FAR", 28.8, 77.5, 10000)     // Large enough
        );
        
        
        DispatchPlanResponseDTO plan = optimizationEngine.optimizeDispatch(orders, vehicles);
        
        
        assertThat(plan.getDispatchPlan()).hasSize(1);
        VehiclePlanDTO vehiclePlan = plan.getDispatchPlan().get(0);
        
        assertThat(vehiclePlan.getVehicleId())
            .as("Should select vehicle with capacity even if farther")
            .isEqualTo("VEH-FAR");
    }
    
    
    @Test
    @DisplayName("Should handle empty orders list")
    void testEmptyOrdersList() {
        
        List<DeliveryOrder> orders = new ArrayList<>();
        
        
        List<Vehicle> vehicles = List.of(
            createVehicle("VEH-001", 28.6, 77.2, 10000)
        );
        
        
        DispatchPlanResponseDTO plan = optimizationEngine.optimizeDispatch(orders, vehicles);
        
        
        assertThat(plan.getStatus()).isEqualTo("FAILED");
        assertThat(plan.getMessage()).contains("No orders");
        assertThat(plan.getDispatchPlan()).isEmpty();
    }
    
    @Test
    @DisplayName("Should handle empty vehicles list")
    void testEmptyVehiclesList() {
        
        List<DeliveryOrder> orders = List.of(
            createOrder("ORD-001", 28.6, 77.2, 5000, Priority.HIGH)
        );
        
        
        List<Vehicle> vehicles = new ArrayList<>();
        
        
        DispatchPlanResponseDTO plan = optimizationEngine.optimizeDispatch(orders, vehicles);
        
        
        assertThat(plan.getStatus()).isEqualTo("FAILED");
        assertThat(plan.getMessage()).contains("No vehicles");
        assertThat(plan.getDispatchPlan()).isEmpty();
    }
    
    @Test
    @DisplayName("Should handle when no vehicle has sufficient capacity")
    void testNoVehicleWithSufficientCapacity() {
        
        List<DeliveryOrder> orders = List.of(
            createOrder("ORD-HEAVY", 28.6, 77.2, 50000, Priority.HIGH)
        );
        
        
        List<Vehicle> vehicles = List.of(
            createVehicle("VEH-001", 28.6, 77.2, 10000),
            createVehicle("VEH-002", 28.61, 77.21, 10000)
        );
        
        
        DispatchPlanResponseDTO plan = optimizationEngine.optimizeDispatch(orders, vehicles);
        
        
        assertThat(plan.getSummary().getAssignedOrders()).isEqualTo(0);
        assertThat(plan.getSummary().getUnassignedOrders()).isEqualTo(1);
        assertThat(plan.getStatus()).isEqualTo("PARTIAL");
    }
    
    @Test
    @DisplayName("Should calculate utilization percentage correctly")
    void testUtilizationCalculation() {
        
        List<DeliveryOrder> orders = List.of(
            createOrder("ORD-001", 28.6, 77.2, 5000, Priority.HIGH),
            createOrder("ORD-002", 28.61, 77.21, 3000, Priority.HIGH)
        );
        
        
        List<Vehicle> vehicles = List.of(
            createVehicle("VEH-001", 28.6, 77.2, 10000)
        );
        
        
        DispatchPlanResponseDTO plan = optimizationEngine.optimizeDispatch(orders, vehicles);
        
       
        VehiclePlanDTO vehiclePlan = plan.getDispatchPlan().get(0);
        assertThat(vehiclePlan.getUtilizationPercentage())
            .as("Utilization percentage")
            .isEqualTo(80.0);
    }
    
    @Test
    @DisplayName("Should track total distance covered")
    void testTotalDistanceTracking() {
        
        List<DeliveryOrder> orders = List.of(
            createOrder("ORD-001", 28.6, 77.2, 2000, Priority.HIGH),
            createOrder("ORD-002", 28.7, 77.3, 2000, Priority.HIGH)
        );
        
        
        List<Vehicle> vehicles = List.of(
            createVehicle("VEH-001", 28.6, 77.2, 10000)
        );
        
        
        DispatchPlanResponseDTO plan = optimizationEngine.optimizeDispatch(orders, vehicles);
        
       
        VehiclePlanDTO vehiclePlan = plan.getDispatchPlan().get(0);
        assertThat(vehiclePlan.getTotalDistance())
            .as("Total distance string")
            .matches("\\d+\\.\\d{2} km");
        
        
        String distanceStr = vehiclePlan.getTotalDistance().replace(" km", "");
        double distance = Double.parseDouble(distanceStr);
        assertThat(distance).isGreaterThan(0);
    }
    
    
    
    
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