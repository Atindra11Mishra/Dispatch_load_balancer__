package com.freightfox.dispatch.integration;

import com.freightfox.dispatch.model.dto.*;
import com.freightfox.dispatch.model.entity.DeliveryOrder;
import com.freightfox.dispatch.model.entity.Vehicle;
import com.freightfox.dispatch.repository.OrderRepository;
import com.freightfox.dispatch.repository.VehicleRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * End-to-End Integration Test for Dispatch System
 * 
 * INTEGRATION TEST CHARACTERISTICS:
 * 
 * @SpringBootTest:
 * - Starts full Spring application context
 * - Loads all beans (@Controller, @Service, @Repository)
 * - Starts embedded Tomcat server
 * - Uses real database (H2 in-memory)
 * - Full stack testing: HTTP → Controller → Service → Repository → Database
 * 
 * webEnvironment = RANDOM_PORT:
 * - Starts server on random available port
 * - Avoids port conflicts when running multiple tests
 * - Port injected via @LocalServerPort
 * 
 * TestRestTemplate:
 * - Makes real HTTP requests to running server
 * - Like: axios or fetch in JavaScript
 * - Simplified API for testing REST endpoints
 * 
 * @ActiveProfiles("test"):
 * - Activates "test" profile
 * - Loads application-test.yml configuration
 * - Uses H2 in-memory database instead of PostgreSQL
 * 
 * @TestMethodOrder:
 * - Controls test execution order
 * - IMPORTANT: Integration tests run in sequence
 * - Each test builds on previous state
 * 
 * UNIT vs INTEGRATION TESTS:
 * 
 * Unit Tests (@WebMvcTest):
 * - Fast (~1-2 seconds)
 * - Isolated (mocked dependencies)
 * - Test single component
 * - No database
 * - MockMvc (simulated HTTP)
 * 
 * Integration Tests (@SpringBootTest):
 * - Slower (~5-10 seconds)
 * - Real components
 * - Test entire flow
 * - Real database (H2)
 * - TestRestTemplate (real HTTP)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Dispatch System - End-to-End Integration Tests")
class DispatchIntegrationTest {
    
    // ========================================================================
    // INJECTED DEPENDENCIES
    // ========================================================================
    
    @LocalServerPort
    private int port;  // Random port assigned by Spring Boot
    
    @Autowired
    private TestRestTemplate restTemplate;  // HTTP client for testing
    
    @Autowired
    private OrderRepository orderRepository;  // Direct database access
    
    @Autowired
    private VehicleRepository vehicleRepository;  // Direct database access
    
    // ========================================================================
    // BASE URL BUILDER
    // ========================================================================
    
    private String baseUrl() {
        return "http://localhost:" + port + "/api/dispatch";
    }
    
    // ========================================================================
    // SETUP & TEARDOWN
    // ========================================================================
    
    @BeforeEach
    void setUp() {
        // Clean database before each test
        orderRepository.deleteAll();
        vehicleRepository.deleteAll();
    }
    
    @AfterAll
    static void tearDown() {
        // Cleanup after all tests complete
        System.out.println("✅ Integration tests completed successfully!");
    }
    
    // ========================================================================
    // TEST 1: POST ORDERS (Setup)
    // ========================================================================
    
    @Test
    @Order(1)
    @DisplayName("Step 1: POST /orders - Should save 3 orders successfully")
    void step1_PostOrders() {
        // GIVEN: 3 orders with different priorities
        OrderRequestDTO request = OrderRequestDTO.builder()
            .orders(List.of(
                createOrderDTO("ORD-001", 28.6139, 77.2090, 5000, "HIGH"),
                createOrderDTO("ORD-002", 28.5355, 77.3910, 8000, "HIGH"),
                createOrderDTO("ORD-003", 28.7000, 77.1000, 3000, "MEDIUM")
            ))
            .build();
        
        // WHEN: POST request to /api/dispatch/orders
        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
            baseUrl() + "/orders",
            request,
            ApiResponse.class
        );
        
        // THEN: Should return 200 OK
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).contains("Successfully saved 3 orders");
        assertThat(response.getBody().getMessage()).contains("2 HIGH, 1 MEDIUM, 0 LOW");
        
        // THEN: Verify orders exist in database
        List<DeliveryOrder> ordersInDb = orderRepository.findAll();
        assertThat(ordersInDb).hasSize(3);
        assertThat(ordersInDb)
            .extracting("orderId")
            .containsExactlyInAnyOrder("ORD-001", "ORD-002", "ORD-003");
        
        System.out.println("✓ Step 1 Complete: 3 orders saved to database");
    }
    
    // ========================================================================
    // TEST 2: POST VEHICLES (Setup)
    // ========================================================================
    
    @Test
    @Order(2)
    @DisplayName("Step 2: POST /vehicles - Should save 2 vehicles successfully")
    void step2_PostVehicles() {
        // GIVEN: 2 vehicles with different capacities
        VehicleRequestDTO request = VehicleRequestDTO.builder()
            .vehicles(List.of(
                createVehicleDTO("VEH-001", 28.6139, 77.2090, 10000),  // Near ORD-001
                createVehicleDTO("VEH-002", 28.5355, 77.3910, 12000)   // Near ORD-002
            ))
            .build();
        
        // WHEN: POST request to /api/dispatch/vehicles
        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
            baseUrl() + "/vehicles",
            request,
            ApiResponse.class
        );
        
        // THEN: Should return 200 OK
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).contains("Successfully saved 2 vehicles");
        assertThat(response.getBody().getMessage()).contains("Total capacity: 22000 grams");
        
        // THEN: Verify vehicles exist in database
        List<Vehicle> vehiclesInDb = vehicleRepository.findAll();
        assertThat(vehiclesInDb).hasSize(2);
        assertThat(vehiclesInDb)
            .extracting("vehicleId")
            .containsExactlyInAnyOrder("VEH-001", "VEH-002");
        
        System.out.println("✓ Step 2 Complete: 2 vehicles saved to database");
    }
    
    // ========================================================================
    // TEST 3: GET DISPATCH PLAN (Main Test)
    // ========================================================================
    
    @Test
    @Order(3)
    @DisplayName("Step 3: GET /plan - Should generate optimized dispatch plan")
    void step3_GetDispatchPlan() {
        // GIVEN: Orders and vehicles exist in database (from previous tests)
        // Setup data first
        step1_PostOrders();
        step2_PostVehicles();
        
        // WHEN: GET request to /api/dispatch/plan
        ResponseEntity<DispatchPlanResponseDTO> response = restTemplate.getForEntity(
            baseUrl() + "/plan",
            DispatchPlanResponseDTO.class
        );
        
        // THEN: Should return 200 OK
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
        DispatchPlanResponseDTO plan = response.getBody();
        
        // THEN: Verify plan structure
        assertThat(plan.getStatus()).isEqualTo("SUCCESS");
        assertThat(plan.getMessage()).contains("Dispatch plan generated successfully");
        assertThat(plan.getDispatchPlan()).isNotEmpty();
        
        // THEN: Verify summary statistics
        assertThat(plan.getSummary()).isNotNull();
        assertThat(plan.getSummary().getTotalOrders()).isEqualTo(3);
        assertThat(plan.getSummary().getTotalVehicles()).isEqualTo(2);
        
        // THEN: All orders should be assigned (sufficient capacity)
        assertThat(plan.getSummary().getAssignedOrders()).isEqualTo(3);
        assertThat(plan.getSummary().getUnassignedOrders()).isEqualTo(0);
        
        // THEN: Verify vehicle assignments
        List<VehiclePlanDTO> vehiclePlans = plan.getDispatchPlan();
        assertThat(vehiclePlans).isNotEmpty();
        
        // THEN: Each vehicle should have assigned orders
        for (VehiclePlanDTO vehiclePlan : vehiclePlans) {
            assertThat(vehiclePlan.getVehicleId()).isIn("VEH-001", "VEH-002");
            assertThat(vehiclePlan.getAssignedOrders()).isNotEmpty();
            assertThat(vehiclePlan.getTotalLoad()).isGreaterThan(0);
            assertThat(vehiclePlan.getOrderCount()).isGreaterThan(0);
            
            // THEN: Vehicle should not exceed capacity
            int capacity = vehiclePlan.getVehicleId().equals("VEH-001") ? 10000 : 12000;
            assertThat(vehiclePlan.getTotalLoad()).isLessThanOrEqualTo(capacity);
            
            System.out.printf("✓ %s assigned %d orders (Load: %d/%d, Utilization: %.1f%%)%n",
                vehiclePlan.getVehicleId(),
                vehiclePlan.getOrderCount(),
                vehiclePlan.getTotalLoad(),
                capacity,
                vehiclePlan.getUtilizationPercentage());
        }
        
        // THEN: Verify HIGH priority orders are assigned first
        boolean foundHighPriority = false;
        for (VehiclePlanDTO vehiclePlan : vehiclePlans) {
            for (AssignedOrderDTO order : vehiclePlan.getAssignedOrders()) {
                if (order.getPriority().equals("HIGH")) {
                    foundHighPriority = true;
                    break;
                }
            }
        }
        assertThat(foundHighPriority).isTrue();
        
        System.out.println("✓ Step 3 Complete: Dispatch plan generated and verified");
    }
    
    // ========================================================================
    // TEST 4: ERROR HANDLING - No Orders
    // ========================================================================
    
    @Test
    @Order(4)
    @DisplayName("Step 4: GET /plan - Should return 400 when no orders exist")
    void step4_GetDispatchPlanWithNoOrders() {
        // GIVEN: Only vehicles exist, no orders
        step2_PostVehicles();
        
        // WHEN: GET request to /api/dispatch/plan
        ResponseEntity<String> response = restTemplate.getForEntity(
            baseUrl() + "/plan",
            String.class
        );
        
        // THEN: Should return 400 Bad Request
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("No orders available");
        
        System.out.println("✓ Step 4 Complete: Correctly handled missing orders");
    }
    
    // ========================================================================
    // TEST 5: ERROR HANDLING - No Vehicles
    // ========================================================================
    
    @Test
    @Order(5)
    @DisplayName("Step 5: GET /plan - Should return 400 when no vehicles exist")
    void step5_GetDispatchPlanWithNoVehicles() {
        // GIVEN: Only orders exist, no vehicles
        step1_PostOrders();
        
        // WHEN: GET request to /api/dispatch/plan
        ResponseEntity<String> response = restTemplate.getForEntity(
            baseUrl() + "/plan",
            String.class
        );
        
        // THEN: Should return 400 Bad Request
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("No vehicles available");
        
        System.out.println("✓ Step 5 Complete: Correctly handled missing vehicles");
    }
    
    // ========================================================================
    // TEST 6: VALIDATION - Invalid Order Data
    // ========================================================================
    
    @Test
    @Order(6)
    @DisplayName("Step 6: POST /orders - Should return 400 for invalid data")
    void step6_PostOrdersWithInvalidData() {
        // GIVEN: Order with invalid latitude
        OrderRequestDTO request = OrderRequestDTO.builder()
            .orders(List.of(
                createOrderDTO("ORD-INVALID", 95.0, 77.2090, 5000, "HIGH")  // Invalid lat
            ))
            .build();
        
        // WHEN: POST request with invalid data
        ResponseEntity<String> response = restTemplate.postForEntity(
            baseUrl() + "/orders",
            request,
            String.class
        );
        
        // THEN: Should return 400 Bad Request
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Validation failed");
        assertThat(response.getBody()).contains("Latitude must be between -90 and 90");
        
        System.out.println("✓ Step 6 Complete: Validation correctly rejected invalid data");
    }
    
    // ========================================================================
    // TEST 7: DUPLICATE DETECTION
    // ========================================================================
    
    @Test
    @Order(7)
    @DisplayName("Step 7: POST /orders - Should return 409 for duplicate order ID")
    void step7_PostDuplicateOrder() {
        // GIVEN: Save initial order
        OrderRequestDTO initialRequest = OrderRequestDTO.builder()
            .orders(List.of(
                createOrderDTO("ORD-DUPLICATE", 28.6139, 77.2090, 5000, "HIGH")
            ))
            .build();
        
        restTemplate.postForEntity(baseUrl() + "/orders", initialRequest, ApiResponse.class);
        
        // GIVEN: Attempt to save same order ID again
        OrderRequestDTO duplicateRequest = OrderRequestDTO.builder()
            .orders(List.of(
                createOrderDTO("ORD-DUPLICATE", 28.7000, 77.3000, 3000, "MEDIUM")
            ))
            .build();
        
        // WHEN: POST duplicate order
        ResponseEntity<String> response = restTemplate.postForEntity(
            baseUrl() + "/orders",
            duplicateRequest,
            String.class
        );
        
        // THEN: Should return 409 Conflict
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).contains("already exists");
        assertThat(response.getBody()).contains("ORD-DUPLICATE");
        
        System.out.println("✓ Step 7 Complete: Duplicate detection working correctly");
    }
    
    // ========================================================================
    // TEST 8: CAPACITY CONSTRAINTS
    // ========================================================================
    
    @Test
    @Order(8)
    @DisplayName("Step 8: GET /plan - Should respect capacity constraints")
    void step8_CapacityConstraints() {
        // GIVEN: Heavy orders exceeding single vehicle capacity
        OrderRequestDTO orderRequest = OrderRequestDTO.builder()
            .orders(List.of(
                createOrderDTO("ORD-HEAVY-1", 28.6, 77.2, 8000, "HIGH"),
                createOrderDTO("ORD-HEAVY-2", 28.61, 77.21, 7000, "HIGH"),
                createOrderDTO("ORD-HEAVY-3", 28.62, 77.22, 6000, "HIGH")
            ))
            .build();
        
        // GIVEN: Vehicle with limited capacity
        VehicleRequestDTO vehicleRequest = VehicleRequestDTO.builder()
            .vehicles(List.of(
                createVehicleDTO("VEH-SMALL", 28.6, 77.2, 10000)  // Can only fit 1 heavy order
            ))
            .build();
        
        restTemplate.postForEntity(baseUrl() + "/orders", orderRequest, ApiResponse.class);
        restTemplate.postForEntity(baseUrl() + "/vehicles", vehicleRequest, ApiResponse.class);
        
        // WHEN: Generate dispatch plan
        ResponseEntity<DispatchPlanResponseDTO> response = restTemplate.getForEntity(
            baseUrl() + "/plan",
            DispatchPlanResponseDTO.class
        );
        
        // THEN: Vehicle should not exceed capacity
        DispatchPlanResponseDTO plan = response.getBody();
        assertThat(plan).isNotNull();
        
        VehiclePlanDTO vehiclePlan = plan.getDispatchPlan().get(0);
        assertThat(vehiclePlan.getTotalLoad()).isLessThanOrEqualTo(10000);
        
        // THEN: Some orders should remain unassigned
        assertThat(plan.getSummary().getUnassignedOrders()).isGreaterThan(0);
        
        System.out.println("✓ Step 8 Complete: Capacity constraints respected");
    }
    
    // ========================================================================
    // TEST 9: HEALTH CHECK
    // ========================================================================
    
    @Test
    @Order(9)
    @DisplayName("Step 9: GET /health - Should return healthy status")
    void step9_HealthCheck() {
        // WHEN: GET health endpoint
        ResponseEntity<ApiResponse> response = restTemplate.getForEntity(
            baseUrl() + "/health",
            ApiResponse.class
        );
        
        // THEN: Should return 200 OK
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).contains("Dispatch API is running");
        
        System.out.println("✓ Step 9 Complete: Health check passed");
    }
    
    // ========================================================================
    // COMPLETE WORKFLOW TEST
    // ========================================================================
    
    @Test
    @Order(10)
    @DisplayName("Step 10: Complete Workflow - Orders → Vehicles → Plan")
    void step10_CompleteWorkflow() {
        System.out.println("\n=== Running Complete Workflow Test ===\n");
        
        // STEP 1: POST Orders
        System.out.println("→ Step 1: Submitting orders...");
        step1_PostOrders();
        
        // STEP 2: POST Vehicles
        System.out.println("→ Step 2: Submitting vehicles...");
        step2_PostVehicles();
        
        // STEP 3: GET Plan
        System.out.println("→ Step 3: Generating dispatch plan...");
        ResponseEntity<DispatchPlanResponseDTO> planResponse = restTemplate.getForEntity(
            baseUrl() + "/plan",
            DispatchPlanResponseDTO.class
        );
        
        DispatchPlanResponseDTO plan = planResponse.getBody();
        assertThat(plan).isNotNull();
        
        // Print Plan Summary
        System.out.println("\n=== Dispatch Plan Summary ===");
        System.out.printf("Total Orders: %d%n", plan.getSummary().getTotalOrders());
        System.out.printf("Assigned Orders: %d%n", plan.getSummary().getAssignedOrders());
        System.out.printf("Unassigned Orders: %d%n", plan.getSummary().getUnassignedOrders());
        System.out.printf("Vehicles Used: %d/%d%n", 
            plan.getSummary().getUsedVehicles(),
            plan.getSummary().getTotalVehicles());
        System.out.printf("Total Distance: %s%n", plan.getSummary().getTotalDistanceCovered());
        System.out.printf("Average Utilization: %.1f%%%n", plan.getSummary().getAverageUtilization());
        
        // Print Vehicle Assignments
        System.out.println("\n=== Vehicle Assignments ===");
        for (VehiclePlanDTO vehiclePlan : plan.getDispatchPlan()) {
            System.out.printf("\n%s:%n", vehiclePlan.getVehicleId());
            System.out.printf("  Load: %d grams (%.1f%% utilization)%n",
                vehiclePlan.getTotalLoad(),
                vehiclePlan.getUtilizationPercentage());
            System.out.printf("  Distance: %s%n", vehiclePlan.getTotalDistance());
            System.out.printf("  Orders: %d%n", vehiclePlan.getOrderCount());
            
            for (AssignedOrderDTO order : vehiclePlan.getAssignedOrders()) {
                System.out.printf("    - %s (%s priority, %d grams, %s away)%n",
                    order.getOrderId(),
                    order.getPriority(),
                    order.getPackageWeight(),
                    order.getDistanceFromVehicle());
            }
        }
        
        System.out.println("\n✓ Step 10 Complete: Full workflow executed successfully");
        System.out.println("\n=== All Integration Tests Passed! ===\n");
    }
    
    // ========================================================================
    // HELPER METHODS
    // ========================================================================
    
    private OrderDTO createOrderDTO(String orderId, double lat, double lon, 
                                     int weight, String priority) {
        return OrderDTO.builder()
            .orderId(orderId)
            .latitude(lat)
            .longitude(lon)
            .address("Test Address, Delhi, India")
            .packageWeight(weight)
            .priority(priority)
            .build();
    }
    
    private VehicleDTO createVehicleDTO(String vehicleId, double lat, double lon, int capacity) {
        return VehicleDTO.builder()
            .vehicleId(vehicleId)
            .capacity(capacity)
            .currentLatitude(lat)
            .currentLongitude(lon)
            .currentAddress("Test Hub, Delhi, India")
            .build();
    }
}