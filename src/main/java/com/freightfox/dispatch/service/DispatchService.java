package com.freightfox.dispatch.service;

import com.freightfox.dispatch.exception.*;
import com.freightfox.dispatch.model.dto.*;
import com.freightfox.dispatch.model.entity.DeliveryOrder;
import com.freightfox.dispatch.model.entity.Vehicle;
import com.freightfox.dispatch.model.enums.Priority;
import com.freightfox.dispatch.repository.OrderRepository;
import com.freightfox.dispatch.repository.VehicleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 
 * 
 * @Service 
 * 
 * @Autowired 
 */
@Slf4j
@Service
public class DispatchService {
    
   
    private final OrderRepository orderRepository;
    private final VehicleRepository vehicleRepository;
    private final OptimizationEngine optimizationEngine;
    
    /**
    
     * @Autowired 
     */
    @Autowired
    public DispatchService(
            OrderRepository orderRepository,
            VehicleRepository vehicleRepository,
            OptimizationEngine optimizationEngine) {
        
        this.orderRepository = orderRepository;
        this.vehicleRepository = vehicleRepository;
        this.optimizationEngine = optimizationEngine;
        
        log.info("DispatchService initialized with dependencies");
    }
    
   
    /**
     *
     * 
     * @param request 
     * @return 
     * @throws DuplicateOrderException
     * @Transactional:
     
     */
    @Transactional
    public ApiResponse saveOrders(OrderRequestDTO request) {
        
        log.info("Saving {} orders to database", request.getOrders().size());
        
       
        validateNoDuplicateOrderIds(request.getOrders());
        
        
        List<DeliveryOrder> orders = request.getOrders().stream()
            .map(this::convertToOrderEntity)
            .collect(Collectors.toList());
        
        
        for (DeliveryOrder order : orders) {
            if (orderRepository.existsById(order.getOrderId())) {
                log.error("Duplicate order ID detected: {}", order.getOrderId());
                throw new DuplicateOrderException(order.getOrderId());
            }
        }
        
       
        List<DeliveryOrder> savedOrders = orderRepository.saveAll(orders);
        
        log.info("Successfully saved {} orders", savedOrders.size());
        
        
        String message = String.format(
            "Successfully saved %d orders (%d HIGH, %d MEDIUM, %d LOW priority)",
            savedOrders.size(),
            countByPriority(savedOrders, Priority.HIGH),
            countByPriority(savedOrders, Priority.MEDIUM),
            countByPriority(savedOrders, Priority.LOW)
        );
        
        return ApiResponse.success(message);
    }
    
    /**
     
     * 
     * @param request
     * @return
     * @throws DuplicateVehicleException 
     */
    @Transactional
    public ApiResponse saveVehicles(VehicleRequestDTO request) {
        
        log.info("Saving {} vehicles to database", request.getVehicles().size());
        
       
        validateNoDuplicateVehicleIds(request.getVehicles());
        
        // Convert DTOs to entities
        List<Vehicle> vehicles = request.getVehicles().stream()
            .map(this::convertToVehicleEntity)
            .collect(Collectors.toList());
        
        // Check for existing vehicles in database
        for (Vehicle vehicle : vehicles) {
            if (vehicleRepository.existsById(vehicle.getVehicleId())) {
                log.error("Duplicate vehicle ID detected: {}", vehicle.getVehicleId());
                throw new DuplicateVehicleException(vehicle.getVehicleId());
            }
        }
        
        // Save all vehicles (batch insert)
        List<Vehicle> savedVehicles = vehicleRepository.saveAll(vehicles);
        
        log.info("Successfully saved {} vehicles", savedVehicles.size());
        
        // Calculate total capacity
        int totalCapacity = savedVehicles.stream()
            .mapToInt(Vehicle::getCapacity)
            .sum();
        
        String message = String.format(
            "Successfully saved %d vehicles (Total capacity: %d grams)",
            savedVehicles.size(),
            totalCapacity
        );
        
        return ApiResponse.success(message);
    }
    
    /**
     
     * 
     * @return 
     * @throws NoOrdersException 
     * @throws NoVehiclesException 
     * 
     * @Transactional(readOnly = true):
     
     */
    @Transactional(readOnly = true)
    public DispatchPlanResponseDTO getDispatchPlan() {
        
        log.info("Generating dispatch plan...");
        
        // STEP 1: Fetch all orders
        List<DeliveryOrder> orders = orderRepository.findAll();
        if (orders.isEmpty()) {
            log.error("No orders found in database");
            throw new NoOrdersException();
        }
        log.info("Fetched {} orders from database", orders.size());
        
        // STEP 2: Fetch all vehicles
        List<Vehicle> vehicles = vehicleRepository.findAll();
        if (vehicles.isEmpty()) {
            log.error("No vehicles found in database");
            throw new NoVehiclesException();
        }
        log.info("Fetched {} vehicles from database", vehicles.size());
        
        // STEP 3: Validate capacity (optional check)
        validateTotalCapacity(orders, vehicles);
        
        // STEP 4: Run optimization algorithm
        log.info("Running optimization algorithm...");
        DispatchPlanResponseDTO plan = optimizationEngine.optimizeDispatch(orders, vehicles);
        
        log.info("Dispatch plan generated: {} vehicles used, {}/{} orders assigned",
            plan.getSummary().getUsedVehicles(),
            plan.getSummary().getAssignedOrders(),
            plan.getSummary().getTotalOrders());
        
        return plan;
    }
    
    /**
     * Get all orders (for debugging/admin)
     */
    @Transactional(readOnly = true)
    public List<DeliveryOrder> getAllOrders() {
        return orderRepository.findAll();
    }
    
    /**
     * Get all vehicles (for debugging/admin)
     */
    @Transactional(readOnly = true)
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }
    
    /**
     * Clear all orders (for testing/reset)
     */
    @Transactional
    public ApiResponse clearAllOrders() {
        long count = orderRepository.count();
        orderRepository.deleteAll();
        log.warn("Deleted all {} orders from database", count);
        return ApiResponse.success(String.format("Deleted %d orders", count));
    }
    
    /**
     * Clear all vehicles (for testing/reset)
     */
    @Transactional
    public ApiResponse clearAllVehicles() {
        long count = vehicleRepository.count();
        vehicleRepository.deleteAll();
        log.warn("Deleted all {} vehicles from database", count);
        return ApiResponse.success(String.format("Deleted %d vehicles", count));
    }
    
   
    private DeliveryOrder convertToOrderEntity(OrderDTO dto) {
        return DeliveryOrder.builder()
            .orderId(dto.getOrderId())
            .latitude(dto.getLatitude())
            .longitude(dto.getLongitude())
            .address(dto.getAddress())
            .packageWeight(dto.getPackageWeight())
            .priority(Priority.valueOf(dto.getPriority()))  // String → Enum
            .build();
       
    }
    
    /**
     * Convert VehicleDTO to Vehicle entity
     */
    private Vehicle convertToVehicleEntity(VehicleDTO dto) {
        return Vehicle.builder()
            .vehicleId(dto.getVehicleId())
            .capacity(dto.getCapacity())
            .currentLatitude(dto.getCurrentLatitude())
            .currentLongitude(dto.getCurrentLongitude())
            .currentAddress(dto.getCurrentAddress())
            .build();
    }
    
    
    private void validateNoDuplicateOrderIds(List<OrderDTO> orders) {
        List<String> orderIds = orders.stream()
            .map(OrderDTO::getOrderId)
            .collect(Collectors.toList());
        
        long uniqueCount = orderIds.stream().distinct().count();
        if (uniqueCount < orderIds.size()) {
            log.error("Duplicate order IDs found in request");
            throw new IllegalArgumentException("Request contains duplicate order IDs");
        }
    }
    
    
    private void validateNoDuplicateVehicleIds(List<VehicleDTO> vehicles) {
        List<String> vehicleIds = vehicles.stream()
            .map(VehicleDTO::getVehicleId)
            .collect(Collectors.toList());
        
        long uniqueCount = vehicleIds.stream().distinct().count();
        if (uniqueCount < vehicleIds.size()) {
            log.error("Duplicate vehicle IDs found in request");
            throw new IllegalArgumentException("Request contains duplicate vehicle IDs");
        }
    }
    
    
    private void validateTotalCapacity(List<DeliveryOrder> orders, List<Vehicle> vehicles) {
        
        int totalOrderWeight = orders.stream()
            .mapToInt(DeliveryOrder::getPackageWeight)
            .sum();
        
        int totalVehicleCapacity = vehicles.stream()
            .mapToInt(Vehicle::getCapacity)
            .sum();
        
        log.info("Capacity check: Orders = {} grams, Fleet = {} grams",
            totalOrderWeight, totalVehicleCapacity);
        
        if (totalOrderWeight > totalVehicleCapacity) {
            log.warn("WARNING: Total order weight ({} g) exceeds total fleet capacity ({} g). " +
                     "Some orders may not be assigned.",
                totalOrderWeight, totalVehicleCapacity);
            
            
        }
    }
    
    
    private long countByPriority(List<DeliveryOrder> orders, Priority priority) {
        return orders.stream()
            .filter(o -> o.getPriority() == priority)
            .count();
    }
}