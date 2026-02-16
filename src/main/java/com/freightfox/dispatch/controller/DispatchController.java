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
 * DispatchController - REST API endpoints for dispatch operations
 * 
 * REST ANNOTATIONS EXPLAINED:
 * 
 * @RestController:
 * - Combines @Controller + @ResponseBody
 * - All methods return JSON automatically (no need for manual serialization)
 * - Like: app.use(express.json()) + res.json() in Express
 * 
 * @RequestMapping:
 * - Base path for all endpoints in this controller
 * - Like: app.use('/api/dispatch', router) in Express
 * 
 * @PostMapping, @GetMapping:
 * - HTTP method + path
 * - Like: router.post('/orders', handler) in Express
 * 
 * @Valid:
 * - Triggers validation on request body
 * - Uses @NotNull, @NotBlank, etc. from DTOs
 * - Throws MethodArgumentNotValidException if invalid
 * 
 * @RequestBody:
 * - Maps JSON request body to Java object
 * - Like: req.body in Express (after express.json() middleware)
 */
@Slf4j
@RestController
@RequestMapping("/dispatch")
public class DispatchController {
    
    // ========================================================================
    // DEPENDENCY INJECTION
    // ========================================================================
    
    private final DispatchService dispatchService;
    
    /**
     * Constructor injection (recommended pattern)
     * Spring automatically injects DispatchService bean
     */
    @Autowired
    public DispatchController(DispatchService dispatchService) {
        this.dispatchService = dispatchService;
        log.info("DispatchController initialized");
    }
    
    // ========================================================================
    // REST ENDPOINTS
    // ========================================================================
    
    /**
     * POST /api/dispatch/orders
     * 
     * Accept and save delivery orders
     * 
     * REQUEST:
     * POST /api/dispatch/orders
     * Content-Type: application/json
     * 
     * {
     *   "orders": [
     *     {
     *       "orderId": "ORD-001",
     *       "latitude": 28.6139,
     *       "longitude": 77.2090,
     *       "address": "Connaught Place, New Delhi",
     *       "packageWeight": 5000,
     *       "priority": "HIGH"
     *     }
     *   ]
     * }
     * 
     * RESPONSE (200 OK):
     * {
     *   "message": "Successfully saved 1 orders (1 HIGH, 0 MEDIUM, 0 LOW priority)",
     *   "status": "SUCCESS",
     *   "timestamp": "2024-02-15T14:30:00"
     * }
     * 
     * VALIDATION ERRORS (400 Bad Request):
     * {
     *   "message": "Validation failed",
     *   "errors": [
     *     "Latitude must be between -90 and 90 degrees",
     *     "Order ID is required"
     *   ]
     * }
     * 
     * @param request OrderRequestDTO with list of orders
     * @return ResponseEntity with ApiResponse
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
        
        // Alternative explicit status:
        // return ResponseEntity.status(HttpStatus.OK).body(response);
        // return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    /**
     * POST /api/dispatch/vehicles
     * 
     * Accept and save vehicle fleet data
     * 
     * REQUEST:
     * POST /api/dispatch/vehicles
     * Content-Type: application/json
     * 
     * {
     *   "vehicles": [
     *     {
     *       "vehicleId": "VEH-001",
     *       "capacity": 10000,
     *       "currentLatitude": 28.6139,
     *       "currentLongitude": 77.2090,
     *       "currentAddress": "Delhi Hub"
     *     }
     *   ]
     * }
     * 
     * RESPONSE (200 OK):
     * {
     *   "message": "Successfully saved 1 vehicles (Total capacity: 10000 grams)",
     *   "status": "SUCCESS",
     *   "timestamp": "2024-02-15T14:30:00"
     * }
     * 
     * @param request VehicleRequestDTO with list of vehicles
     * @return ResponseEntity with ApiResponse
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
     * GET /api/dispatch/plan
     * 
     * Generate and return optimized dispatch plan
     * 
     * REQUEST:
     * GET /api/dispatch/plan
     * 
     * RESPONSE (200 OK):
     * {
     *   "message": "Dispatch plan generated successfully",
     *   "status": "SUCCESS",
     *   "dispatchPlan": [
     *     {
     *       "vehicleId": "VEH-001",
     *       "totalLoad": 8000,
     *       "totalDistance": "15.50 km",
     *       "utilizationPercentage": 80.0,
     *       "orderCount": 2,
     *       "assignedOrders": [
     *         {
     *           "orderId": "ORD-001",
     *           "address": "Connaught Place, New Delhi",
     *           "packageWeight": 5000,
     *           "priority": "HIGH",
     *           "distanceFromVehicle": "4.20 km"
     *         }
     *       ]
     *     }
     *   ],
     *   "summary": {
     *     "totalOrders": 5,
     *     "assignedOrders": 5,
     *     "unassignedOrders": 0,
     *     "totalVehicles": 3,
     *     "usedVehicles": 2,
     *     "totalDistanceCovered": "45.80 km",
     *     "averageUtilization": 75.5
     *   }
     * }
     * 
     * ERROR (404 Not Found):
     * {
     *   "message": "No orders available in the system for dispatch planning",
     *   "status": "ERROR",
     *   "timestamp": "2024-02-15T14:30:00"
     * }
     * 
     * @return ResponseEntity with DispatchPlanResponseDTO
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
    
    // ========================================================================
    // ADDITIONAL ENDPOINTS (Optional - for debugging/admin)
    // ========================================================================
    
    /**
     * DELETE /api/dispatch/orders
     * 
     * Clear all orders from database (for testing/reset)
     * 
     * CAUTION: Use only in development/testing
     */
    @DeleteMapping("/orders")
    public ResponseEntity<ApiResponse> clearOrders() {
        log.warn("DELETE /api/dispatch/orders - Clearing all orders");
        
        ApiResponse response = dispatchService.clearAllOrders();
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * DELETE /api/dispatch/vehicles
     * 
     * Clear all vehicles from database (for testing/reset)
     * 
     * CAUTION: Use only in development/testing
     */
    @DeleteMapping("/vehicles")
    public ResponseEntity<ApiResponse> clearVehicles() {
        log.warn("DELETE /api/dispatch/vehicles - Clearing all vehicles");
        
        ApiResponse response = dispatchService.clearAllVehicles();
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/dispatch/health
     * 
     * Health check endpoint
     * 
     * Returns simple status to verify API is running
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse> healthCheck() {
        log.debug("GET /api/dispatch/health - Health check");
        
        ApiResponse response = ApiResponse.success("Dispatch API is running");
        
        return ResponseEntity.ok(response);
    }
}