package com.freightfox.dispatch.controller;

import com.freightfox.dispatch.model.dto.*;
import com.freightfox.dispatch.service.DispatchService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 
 * 
 * @RestController:
 
 * 
 * @RequestMapping:
 
 * 
 * @Valid:
 
 * 
 * @RequestBody:
 
 */
@Slf4j
@RestController
@RequestMapping("/dispatch")
public class DispatchController {
    
    
    
    private final DispatchService dispatchService;
    
    /**
     
     */
    @Autowired
    public DispatchController(DispatchService dispatchService) {
        this.dispatchService = dispatchService;
        log.info("DispatchController initialized");
    }
    
   
    
    /**
    
     * @param request 
     * @return 
     */
    @PostMapping("/orders")
    public ResponseEntity<ApiResponse> acceptOrders(
            @Valid @RequestBody OrderRequestDTO request) {
        
        log.info("POST /api/dispatch/orders - Received {} orders", 
            request.getOrders().size());
        
        // Log first order ID for tracing
        if (!request.getOrders().isEmpty()) {
            log.debug("First order ID: {}", request.getOrders().get(0).getOrderId());
        }
        
        // Call service layer
        ApiResponse response = dispatchService.saveOrders(request);
        
        log.info("Orders saved successfully: {}", response.getMessage());
        
        // Return 200 OK with response
        return ResponseEntity.ok(response);
        
        
    }
    
    /**
     
     * 
     * @param request 
     * @return 
     */
    @PostMapping("/vehicles")
    public ResponseEntity<ApiResponse> acceptVehicles(
            @Valid @RequestBody VehicleRequestDTO request) {
        
        log.info("POST /api/dispatch/vehicles - Received {} vehicles", 
            request.getVehicles().size());
        
        // Log first vehicle ID for tracing
        if (!request.getVehicles().isEmpty()) {
            log.debug("First vehicle ID: {}", request.getVehicles().get(0).getVehicleId());
        }
        
        // Call service layer
        ApiResponse response = dispatchService.saveVehicles(request);
        
        log.info("Vehicles saved successfully: {}", response.getMessage());
        
        return ResponseEntity.ok(response);
    }
    
    /**
    
     * @return 
     */
    @GetMapping("/plan")
    public ResponseEntity<DispatchPlanResponseDTO> getDispatchPlan() {
        
        log.info("GET /api/dispatch/plan - Generating dispatch plan");
        
        // Call service layer (may throw NoOrdersException, NoVehiclesException)
        DispatchPlanResponseDTO plan = dispatchService.getDispatchPlan();
        
        log.info("Dispatch plan generated: {}/{} orders assigned, {} vehicles used",
            plan.getSummary().getAssignedOrders(),
            plan.getSummary().getTotalOrders(),
            plan.getSummary().getUsedVehicles());
        
        return ResponseEntity.ok(plan);
    }
    
   
    
    
    @DeleteMapping("/orders")
    public ResponseEntity<ApiResponse> clearOrders() {
        log.warn("DELETE /api/dispatch/orders - Clearing all orders");
        
        ApiResponse response = dispatchService.clearAllOrders();
        
        return ResponseEntity.ok(response);
    }
    
    
    @DeleteMapping("/vehicles")
    public ResponseEntity<ApiResponse> clearVehicles() {
        log.warn("DELETE /api/dispatch/vehicles - Clearing all vehicles");
        
        ApiResponse response = dispatchService.clearAllVehicles();
        
        return ResponseEntity.ok(response);
    }
    
   
    @GetMapping("/health")
    public ResponseEntity<ApiResponse> healthCheck() {
        log.debug("GET /api/dispatch/health - Health check");
        
        ApiResponse response = ApiResponse.success("Dispatch API is running");
        
        return ResponseEntity.ok(response);
    }
}