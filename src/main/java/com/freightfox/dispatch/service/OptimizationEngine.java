package com.freightfox.dispatch.service;

import com.freightfox.dispatch.model.dto.*;
import com.freightfox.dispatch.model.entity.DeliveryOrder;
import com.freightfox.dispatch.model.entity.Vehicle;
import com.freightfox.dispatch.model.enums.Priority;
import com.freightfox.dispatch.util.HaversineCalculator;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * OptimizationEngine - Core dispatch optimization algorithm
 * 
 * ALGORITHM: Priority-based Greedy Assignment with Distance Minimization
 * 
 * Goal: Assign orders to vehicles such that:
 * 1. High-priority orders are processed first
 * 2. Each vehicle stays within capacity
 * 3. Total distance is minimized (nearest vehicle preference)
 * 
 * Complexity: O(N × M) where N = orders, M = vehicles
 * - Sorting: O(N log N)
 * - Distance matrix: O(N × M)
 * - Assignment: O(N × M) worst case
 */
@Slf4j
@Service
public class OptimizationEngine {
    
    /**
     * Main optimization method - assigns orders to vehicles
     * 
     * @param orders List of delivery orders to assign
     * @param vehicles List of available vehicles
     * @return DispatchPlanResponseDTO with optimized assignments
     */
    public DispatchPlanResponseDTO optimizeDispatch(
            List<DeliveryOrder> orders, 
            List<Vehicle> vehicles) {
        
        log.info("Starting dispatch optimization: {} orders, {} vehicles", 
            orders.size(), vehicles.size());
        
        // Edge case: No orders or no vehicles
        if (orders.isEmpty()) {
            log.warn("No orders to assign");
            return buildEmptyPlan("No orders to assign");
        }
        
        if (vehicles.isEmpty()) {
            log.error("No vehicles available for assignment");
            return buildEmptyPlan("No vehicles available");
        }
        
        // STEP 1: Sort orders by priority, then by weight (descending)
        List<DeliveryOrder> sortedOrders = sortOrdersByPriority(orders);
        log.info("Orders sorted: {} HIGH, {} MEDIUM, {} LOW priority",
            countByPriority(sortedOrders, Priority.HIGH),
            countByPriority(sortedOrders, Priority.MEDIUM),
            countByPriority(sortedOrders, Priority.LOW));
        
        // STEP 2: Initialize vehicle states (tracks current load per vehicle)
        Map<String, VehicleState> vehicleStates = initializeVehicleStates(vehicles);
        
        // STEP 3: Build distance matrix (pre-compute all vehicle-to-order distances)
        Map<String, Double> distanceMatrix = buildDistanceMatrix(vehicles, sortedOrders);
        log.info("Distance matrix built: {} entries cached", distanceMatrix.size());
        
        // STEP 4: Assign orders to vehicles using greedy algorithm
        Map<String, List<AssignmentRecord>> assignments = assignOrders(
            sortedOrders, 
            vehicleStates, 
            distanceMatrix
        );
        
        // STEP 5: Build response DTO
        DispatchPlanResponseDTO response = buildDispatchPlan(
            assignments, 
            vehicleStates, 
            sortedOrders.size()
        );
        
        log.info("Optimization complete: {}/{} orders assigned, {} vehicles used",
            response.getSummary().getAssignedOrders(),
            response.getSummary().getTotalOrders(),
            response.getSummary().getUsedVehicles());
        
        return response;
    }
    
    /**
     * STEP 1: Sort orders by priority (HIGH → MEDIUM → LOW), then by weight
     * 
     * Sorting logic:
     * - Primary: Priority (HIGH=3, MEDIUM=2, LOW=1) descending
     * - Secondary: Weight (heavier first) descending
     * 
     * Why heavier first? Harder to place heavy orders later when vehicles fill up
     */
    private List<DeliveryOrder> sortOrdersByPriority(List<DeliveryOrder> orders) {
        return orders.stream()
            .sorted(Comparator
                // Sort by priority (HIGH first) - negate so higher values come first
                .comparingInt((DeliveryOrder o) -> -o.getPriority().getSortOrder())
                // Then by weight (heavier first)
                .thenComparingInt(o -> -o.getPackageWeight())
            )
            .collect(Collectors.toList());
    }
    
    /**
     * STEP 2: Initialize vehicle states
     * 
     * VehicleState tracks:
     * - Current load (starts at 0)
     * - Remaining capacity
     * - Total distance traveled
     * - Assigned orders
     */
    private Map<String, VehicleState> initializeVehicleStates(List<Vehicle> vehicles) {
        Map<String, VehicleState> states = new HashMap<>();
        
        for (Vehicle vehicle : vehicles) {
            VehicleState state = new VehicleState(vehicle);
            states.put(vehicle.getVehicleId(), state);
        }
        
        log.debug("Initialized {} vehicle states", states.size());
        return states;
    }
    
    /**
     * STEP 3: Build distance matrix (CRITICAL FOR PERFORMANCE)
     * 
     * Pre-computes ALL vehicle-to-order distances
     * Key format: "vehicleId:orderId"
     * 
     * Why cache?
     * - Haversine is computationally expensive (trigonometric functions)
     * - Without caching: O(N × M × iterations) recalculations
     * - With caching: O(N × M) one-time computation
     * 
     * Memory cost: N × M × 8 bytes (double)
     * Example: 100 orders × 10 vehicles = 1,000 doubles = ~8 KB
     */
    private Map<String, Double> buildDistanceMatrix(
            List<Vehicle> vehicles, 
            List<DeliveryOrder> orders) {
        
        Map<String, Double> distanceMatrix = new HashMap<>();
        
        for (Vehicle vehicle : vehicles) {
            for (DeliveryOrder order : orders) {
                
                // Calculate distance once
                double distance = HaversineCalculator.calculate(
                    vehicle.getCurrentLatitude(),
                    vehicle.getCurrentLongitude(),
                    order.getLatitude(),
                    order.getLongitude()
                );
                
                // Cache with composite key
                String key = buildDistanceKey(vehicle.getVehicleId(), order.getOrderId());
                distanceMatrix.put(key, distance);
            }
        }
        
        return distanceMatrix;
    }
    
    /**
     * STEP 4: Assign orders to vehicles (GREEDY ALGORITHM)
     * 
     * For each order (in priority order):
     * 1. Find eligible vehicles (capacity >= order weight)
     * 2. Calculate score for each eligible vehicle
     * 3. Assign to vehicle with best score (lowest distance)
     * 4. Update vehicle state (load, distance)
     * 
     * Score = distance (lower is better)
     * Future enhancement: score = α × distance + β × utilization
     */
    private Map<String, List<AssignmentRecord>> assignOrders(
            List<DeliveryOrder> sortedOrders,
            Map<String, VehicleState> vehicleStates,
            Map<String, Double> distanceMatrix) {
        
        // Assignments map: vehicleId → List of assigned orders
        Map<String, List<AssignmentRecord>> assignments = new HashMap<>();
        
        // Initialize empty lists for all vehicles
        for (String vehicleId : vehicleStates.keySet()) {
            assignments.put(vehicleId, new ArrayList<>());
        }
        
        int assignedCount = 0;
        int unassignedCount = 0;
        
        // Process each order
        for (DeliveryOrder order : sortedOrders) {
            
            log.debug("Processing order {}: {} priority, {} grams",
                order.getOrderId(), order.getPriority(), order.getPackageWeight());
            
            // Find best vehicle for this order
            VehicleAssignment bestAssignment = findBestVehicle(
                order, 
                vehicleStates, 
                distanceMatrix
            );
            
            if (bestAssignment != null) {
                // Assign order to vehicle
                String vehicleId = bestAssignment.getVehicleId();
                VehicleState state = vehicleStates.get(vehicleId);
                
                // Update vehicle state
                state.addOrder(order, bestAssignment.getDistance());
                
                // Record assignment
                AssignmentRecord record = new AssignmentRecord(
                    order,
                    bestAssignment.getDistance()
                );
                assignments.get(vehicleId).add(record);
                
                assignedCount++;
                
                log.info("✓ Assigned order {} to vehicle {} (distance: {:.2f} km, load: {}/{} grams)",
                    order.getOrderId(),
                    vehicleId,
                    bestAssignment.getDistance(),
                    state.getCurrentLoad(),
                    state.getVehicle().getCapacity());
                
            } else {
                // No suitable vehicle found
                unassignedCount++;
                
                log.warn("✗ Cannot assign order {}: {} priority, {} grams - No vehicle with sufficient capacity",
                    order.getOrderId(),
                    order.getPriority(),
                    order.getPackageWeight());
            }
        }
        
        log.info("Assignment complete: {} assigned, {} unassigned", 
            assignedCount, unassignedCount);
        
        return assignments;
    }
    
    /**
     * Find best vehicle for an order
     * 
     * Selection criteria:
     * 1. Vehicle must have capacity >= order weight
     * 2. Among eligible vehicles, choose nearest one
     * 
     * @return VehicleAssignment with vehicleId and distance, or null if no vehicle available
     */
    private VehicleAssignment findBestVehicle(
            DeliveryOrder order,
            Map<String, VehicleState> vehicleStates,
            Map<String, Double> distanceMatrix) {
        
        VehicleAssignment best = null;
        double minDistance = Double.MAX_VALUE;
        
        for (Map.Entry<String, VehicleState> entry : vehicleStates.entrySet()) {
            String vehicleId = entry.getKey();
            VehicleState state = entry.getValue();
            
            // Check capacity constraint
            if (state.getRemainingCapacity() >= order.getPackageWeight()) {
                
                // Get cached distance
                String key = buildDistanceKey(vehicleId, order.getOrderId());
                double distance = distanceMatrix.get(key);
                
                // Update best if this vehicle is nearer
                if (distance < minDistance) {
                    minDistance = distance;
                    best = new VehicleAssignment(vehicleId, distance);
                }
            }
        }
        
        return best;
    }
    
    /**
     * STEP 5: Build final dispatch plan response
     */
    private DispatchPlanResponseDTO buildDispatchPlan(
            Map<String, List<AssignmentRecord>> assignments,
            Map<String, VehicleState> vehicleStates,
            int totalOrders) {
        
        List<VehiclePlanDTO> dispatchPlan = new ArrayList<>();
        int assignedOrders = 0;
        int usedVehicles = 0;
        double totalDistanceCovered = 0.0;
        double totalUtilization = 0.0;
        
        // Build vehicle plans
        for (Map.Entry<String, VehicleState> entry : vehicleStates.entrySet()) {
            String vehicleId = entry.getKey();
            VehicleState state = entry.getValue();
            List<AssignmentRecord> vehicleOrders = assignments.get(vehicleId);
            
            // Skip vehicles with no assignments
            if (vehicleOrders.isEmpty()) {
                continue;
            }
            
            usedVehicles++;
            assignedOrders += vehicleOrders.size();
            totalDistanceCovered += state.getTotalDistance();
            
            // Calculate utilization percentage
            double utilization = (state.getCurrentLoad() * 100.0) / state.getVehicle().getCapacity();
            totalUtilization += utilization;
            
            // Build assigned orders DTOs
            List<AssignedOrderDTO> assignedOrderDTOs = vehicleOrders.stream()
                .map(record -> AssignedOrderDTO.builder()
                    .orderId(record.getOrder().getOrderId())
                    .address(record.getOrder().getAddress())
                    .packageWeight(record.getOrder().getPackageWeight())
                    .priority(record.getOrder().getPriority().name())
                    .distanceFromVehicle(String.format("%.2f km", record.getDistance()))
                    .build())
                .collect(Collectors.toList());
            
            // Build vehicle plan
            VehiclePlanDTO vehiclePlan = VehiclePlanDTO.builder()
                .vehicleId(vehicleId)
                .totalLoad(state.getCurrentLoad())
                .totalDistance(String.format("%.2f km", state.getTotalDistance()))
                .assignedOrders(assignedOrderDTOs)
                .orderCount(vehicleOrders.size())
                .utilizationPercentage(Math.round(utilization * 100.0) / 100.0)
                .build();
            
            dispatchPlan.add(vehiclePlan);
        }
        
        // Calculate averages
        double averageUtilization = usedVehicles > 0 
            ? Math.round((totalUtilization / usedVehicles) * 100.0) / 100.0 
            : 0.0;
        
        // Build summary
        PlanSummaryDTO summary = PlanSummaryDTO.builder()
            .totalOrders(totalOrders)
            .assignedOrders(assignedOrders)
            .unassignedOrders(totalOrders - assignedOrders)
            .totalVehicles(vehicleStates.size())
            .usedVehicles(usedVehicles)
            .totalDistanceCovered(String.format("%.2f km", totalDistanceCovered))
            .averageUtilization(averageUtilization)
            .build();
        
        // Build response
        return DispatchPlanResponseDTO.builder()
            .message("Dispatch plan generated successfully")
            .status(assignedOrders == totalOrders ? "SUCCESS" : "PARTIAL")
            .dispatchPlan(dispatchPlan)
            .summary(summary)
            .build();
    }
    
    /**
     * Build empty plan for edge cases
     */
    private DispatchPlanResponseDTO buildEmptyPlan(String message) {
        return DispatchPlanResponseDTO.builder()
            .message(message)
            .status("FAILED")
            .dispatchPlan(new ArrayList<>())
            .summary(PlanSummaryDTO.builder()
                .totalOrders(0)
                .assignedOrders(0)
                .unassignedOrders(0)
                .totalVehicles(0)
                .usedVehicles(0)
                .totalDistanceCovered("0.00 km")
                .averageUtilization(0.0)
                .build())
            .build();
    }
    
    // ========================================================================
    // HELPER METHODS
    // ========================================================================
    
    /**
     * Build composite key for distance matrix
     * Format: "vehicleId:orderId"
     */
    private String buildDistanceKey(String vehicleId, String orderId) {
        return vehicleId + ":" + orderId;
    }
    
    /**
     * Count orders by priority
     */
    private long countByPriority(List<DeliveryOrder> orders, Priority priority) {
        return orders.stream()
            .filter(o -> o.getPriority() == priority)
            .count();
    }
    
    // ========================================================================
    // INNER CLASSES (Data Structures)
    // ========================================================================
    
    /**
     * VehicleState - Tracks current state of a vehicle during assignment
     * 
     * Mutable state object (updated as orders are assigned)
     */
    @Data
    private static class VehicleState {
        private final Vehicle vehicle;
        private int currentLoad = 0;
        private double totalDistance = 0.0;
        
        public VehicleState(Vehicle vehicle) {
            this.vehicle = vehicle;
        }
        
        public int getRemainingCapacity() {
            return vehicle.getCapacity() - currentLoad;
        }
        
        public void addOrder(DeliveryOrder order, double distance) {
            this.currentLoad += order.getPackageWeight();
            this.totalDistance += distance;
        }
    }
    
    /**
     * AssignmentRecord - Records an order-to-vehicle assignment
     */
    @Data
    private static class AssignmentRecord {
        private final DeliveryOrder order;
        private final double distance;
    }
    
    /**
     * VehicleAssignment - Candidate vehicle for an order
     */
    @Data
    private static class VehicleAssignment {
        private final String vehicleId;
        private final double distance;
    }
    
}