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
 
 * @SpringBootTest:

 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Dispatch System - End-to-End Integration Tests")
class DispatchIntegrationTest {
    
   
    @LocalServerPort
    private int port;  
    @Autowired
    private TestRestTemplate restTemplate;  
    
    @Autowired
    private OrderRepository orderRepository;  
    @Autowired
    private VehicleRepository vehicleRepository; 
    private String baseUrl() {
        return "http://localhost:" + port + "/api/dispatch";
    }
    
    
    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        vehicleRepository.deleteAll();
    }
    
    @AfterAll
    static void tearDown() {
        System.out.println("✅ Integration tests completed successfully!");
    }
    
   
    
    @Test
    @Order(1)
    @DisplayName("Step 1: POST /orders - Should save 3 orders successfully")
    void step1_PostOrders() {
        
        OrderRequestDTO request = OrderRequestDTO.builder()
            .orders(List.of(
                createOrderDTO("ORD-001", 28.6139, 77.2090, 5000, "HIGH"),
                createOrderDTO("ORD-002", 28.5355, 77.3910, 8000, "HIGH"),
                createOrderDTO("ORD-003", 28.7000, 77.1000, 3000, "MEDIUM")
            ))
            .build();
        
     
        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
            baseUrl() + "/orders",
            request,
            ApiResponse.class
        );
        
      
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).contains("Successfully saved 3 orders");
        assertThat(response.getBody().getMessage()).contains("2 HIGH, 1 MEDIUM, 0 LOW");
        
       
        List<DeliveryOrder> ordersInDb = orderRepository.findAll();
        assertThat(ordersInDb).hasSize(3);
        assertThat(ordersInDb)
            .extracting("orderId")
            .containsExactlyInAnyOrder("ORD-001", "ORD-002", "ORD-003");
        
        System.out.println("✓ Step 1 Complete: 3 orders saved to database");
    }
   
    @Test
    @Order(2)
    @DisplayName("Step 2: POST /vehicles - Should save 2 vehicles successfully")
    void step2_PostVehicles() {
       
        VehicleRequestDTO request = VehicleRequestDTO.builder()
            .vehicles(List.of(
                createVehicleDTO("VEH-001", 28.6139, 77.2090, 10000),  // Near ORD-001
                createVehicleDTO("VEH-002", 28.5355, 77.3910, 12000)   // Near ORD-002
            ))
            .build();
        
      
        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
            baseUrl() + "/vehicles",
            request,
            ApiResponse.class
        );
        
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).contains("Successfully saved 2 vehicles");
        assertThat(response.getBody().getMessage()).contains("Total capacity: 22000 grams");
        
       
        List<Vehicle> vehiclesInDb = vehicleRepository.findAll();
        assertThat(vehiclesInDb).hasSize(2);
        assertThat(vehiclesInDb)
            .extracting("vehicleId")
            .containsExactlyInAnyOrder("VEH-001", "VEH-002");
        
        System.out.println("✓ Step 2 Complete: 2 vehicles saved to database");
    }
    
  
    @Test
    @Order(3)
    @DisplayName("Step 3: GET /plan - Should generate optimized dispatch plan")
    void step3_GetDispatchPlan() {
        
        step1_PostOrders();
        step2_PostVehicles();
        
        
        ResponseEntity<DispatchPlanResponseDTO> response = restTemplate.getForEntity(
            baseUrl() + "/plan",
            DispatchPlanResponseDTO.class
        );
        
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
        DispatchPlanResponseDTO plan = response.getBody();
        
        
        assertThat(plan.getStatus()).isEqualTo("SUCCESS");
        assertThat(plan.getMessage()).contains("Dispatch plan generated successfully");
        assertThat(plan.getDispatchPlan()).isNotEmpty();
        
        
        assertThat(plan.getSummary()).isNotNull();
        assertThat(plan.getSummary().getTotalOrders()).isEqualTo(3);
        assertThat(plan.getSummary().getTotalVehicles()).isEqualTo(2);
        
        
        assertThat(plan.getSummary().getAssignedOrders()).isEqualTo(3);
        assertThat(plan.getSummary().getUnassignedOrders()).isEqualTo(0);
        
        
        List<VehiclePlanDTO> vehiclePlans = plan.getDispatchPlan();
        assertThat(vehiclePlans).isNotEmpty();
        
       
        for (VehiclePlanDTO vehiclePlan : vehiclePlans) {
            assertThat(vehiclePlan.getVehicleId()).isIn("VEH-001", "VEH-002");
            assertThat(vehiclePlan.getAssignedOrders()).isNotEmpty();
            assertThat(vehiclePlan.getTotalLoad()).isGreaterThan(0);
            assertThat(vehiclePlan.getOrderCount()).isGreaterThan(0);
            
           
            int capacity = vehiclePlan.getVehicleId().equals("VEH-001") ? 10000 : 12000;
            assertThat(vehiclePlan.getTotalLoad()).isLessThanOrEqualTo(capacity);
            
            System.out.printf("✓ %s assigned %d orders (Load: %d/%d, Utilization: %.1f%%)%n",
                vehiclePlan.getVehicleId(),
                vehiclePlan.getOrderCount(),
                vehiclePlan.getTotalLoad(),
                capacity,
                vehiclePlan.getUtilizationPercentage());
        }
        
        
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
    
    
    @Test
    @Order(4)
    @DisplayName("Step 4: GET /plan - Should return 400 when no orders exist")
    void step4_GetDispatchPlanWithNoOrders() {
       
        step2_PostVehicles();
        
       
        ResponseEntity<String> response = restTemplate.getForEntity(
            baseUrl() + "/plan",
            String.class
        );
        
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("No orders available");
        
        System.out.println("✓ Step 4 Complete: Correctly handled missing orders");
    }
    
   
    
    @Test
    @Order(5)
    @DisplayName("Step 5: GET /plan - Should return 400 when no vehicles exist")
    void step5_GetDispatchPlanWithNoVehicles() {
        
        step1_PostOrders();
        
        
        ResponseEntity<String> response = restTemplate.getForEntity(
            baseUrl() + "/plan",
            String.class
        );
        
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("No vehicles available");
        
        System.out.println("✓ Step 5 Complete: Correctly handled missing vehicles");
    }
    
    
    
    @Test
    @Order(6)
    @DisplayName("Step 6: POST /orders - Should return 400 for invalid data")
    void step6_PostOrdersWithInvalidData() {
        
        OrderRequestDTO request = OrderRequestDTO.builder()
            .orders(List.of(
                createOrderDTO("ORD-INVALID", 95.0, 77.2090, 5000, "HIGH")  
            ))
            .build();
        
        
        ResponseEntity<String> response = restTemplate.postForEntity(
            baseUrl() + "/orders",
            request,
            String.class
        );
        
       
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Validation failed");
        assertThat(response.getBody()).contains("Latitude must be between -90 and 90");
        
        System.out.println("✓ Step 6 Complete: Validation correctly rejected invalid data");
    }
    
   
    
    @Test
    @Order(7)
    @DisplayName("Step 7: POST /orders - Should return 409 for duplicate order ID")
    void step7_PostDuplicateOrder() {
      
        OrderRequestDTO initialRequest = OrderRequestDTO.builder()
            .orders(List.of(
                createOrderDTO("ORD-DUPLICATE", 28.6139, 77.2090, 5000, "HIGH")
            ))
            .build();
        
        restTemplate.postForEntity(baseUrl() + "/orders", initialRequest, ApiResponse.class);
        
      
        OrderRequestDTO duplicateRequest = OrderRequestDTO.builder()
            .orders(List.of(
                createOrderDTO("ORD-DUPLICATE", 28.7000, 77.3000, 3000, "MEDIUM")
            ))
            .build();
        
        
        ResponseEntity<String> response = restTemplate.postForEntity(
            baseUrl() + "/orders",
            duplicateRequest,
            String.class
        );
        
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).contains("already exists");
        assertThat(response.getBody()).contains("ORD-DUPLICATE");
        
        System.out.println("✓ Step 7 Complete: Duplicate detection working correctly");
    }
    
 
    @Test
    @Order(8)
    @DisplayName("Step 8: GET /plan - Should respect capacity constraints")
    void step8_CapacityConstraints() {
       
        OrderRequestDTO orderRequest = OrderRequestDTO.builder()
            .orders(List.of(
                createOrderDTO("ORD-HEAVY-1", 28.6, 77.2, 8000, "HIGH"),
                createOrderDTO("ORD-HEAVY-2", 28.61, 77.21, 7000, "HIGH"),
                createOrderDTO("ORD-HEAVY-3", 28.62, 77.22, 6000, "HIGH")
            ))
            .build();
        
       
        VehicleRequestDTO vehicleRequest = VehicleRequestDTO.builder()
            .vehicles(List.of(
                createVehicleDTO("VEH-SMALL", 28.6, 77.2, 10000)  
            ))
            .build();
        
        restTemplate.postForEntity(baseUrl() + "/orders", orderRequest, ApiResponse.class);
        restTemplate.postForEntity(baseUrl() + "/vehicles", vehicleRequest, ApiResponse.class);
        
        
        ResponseEntity<DispatchPlanResponseDTO> response = restTemplate.getForEntity(
            baseUrl() + "/plan",
            DispatchPlanResponseDTO.class
        );
        
      
        DispatchPlanResponseDTO plan = response.getBody();
        assertThat(plan).isNotNull();
        
        VehiclePlanDTO vehiclePlan = plan.getDispatchPlan().get(0);
        assertThat(vehiclePlan.getTotalLoad()).isLessThanOrEqualTo(10000);
        
       
        assertThat(plan.getSummary().getUnassignedOrders()).isGreaterThan(0);
        
        System.out.println("✓ Step 8 Complete: Capacity constraints respected");
    }
    
  
    @Test
    @Order(9)
    @DisplayName("Step 9: GET /health - Should return healthy status")
    void step9_HealthCheck() {
       
        ResponseEntity<ApiResponse> response = restTemplate.getForEntity(
            baseUrl() + "/health",
            ApiResponse.class
        );
        
       
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).contains("Dispatch API is running");
        
        System.out.println("✓ Step 9 Complete: Health check passed");
    }
    
   
    
    @Test
    @Order(10)
    @DisplayName("Step 10: Complete Workflow - Orders → Vehicles → Plan")
    void step10_CompleteWorkflow() {
        System.out.println("\n=== Running Complete Workflow Test ===\n");
        
       
        System.out.println("→ Step 1: Submitting orders...");
        step1_PostOrders();
        
       
        step2_PostVehicles();
        
       
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