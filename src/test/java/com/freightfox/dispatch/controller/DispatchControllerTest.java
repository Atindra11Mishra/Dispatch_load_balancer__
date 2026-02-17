package com.freightfox.dispatch.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightfox.dispatch.exception.NoOrdersException;
import com.freightfox.dispatch.exception.NoVehiclesException;
import com.freightfox.dispatch.model.dto.*;
import com.freightfox.dispatch.service.DispatchService;
import com.freightfox.dispatch.service.OptimizationEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 
 * 
 * @WebMvcTest:

 * 
 * @WebMvcTest
 * 
 * @WebMvcTest:
 * -
 * 
 * @SpringBootTest:
 * 
 */
@WebMvcTest(DispatchController.class)
@DisplayName("DispatchController - REST API Tests")
class DispatchControllerTest {
    
   
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;  // For JSON serialization
    
    
    @MockBean
    private DispatchService dispatchService;
    
    
    
    @Test
    @DisplayName("POST /orders - Should return 200 OK with valid request")
    void testAcceptOrdersSuccess() throws Exception {
       
        OrderRequestDTO request = createValidOrderRequest();
        
        
        ApiResponse mockResponse = ApiResponse.success(
            "Successfully saved 2 orders (1 HIGH, 1 MEDIUM, 0 LOW priority)"
        );
        when(dispatchService.saveOrders(any(OrderRequestDTO.class)))
            .thenReturn(mockResponse);
        
       
        mockMvc.perform(post("/dispatch/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())  
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value(containsString("Successfully saved")))
            .andExpect(jsonPath("$.message").value(containsString("2 orders")));

        
        verify(dispatchService, times(1)).saveOrders(any(OrderRequestDTO.class));
    }
    
    @Test
    @DisplayName("POST /orders - Should return 400 Bad Request for invalid data")
    void testAcceptOrdersWithInvalidData() throws Exception {
        
        String invalidJson = """
            {
              "orders": [
                {
                  "orderId": "",
                  "latitude": 95.0,
                  "longitude": null,
                  "address": "Short",
                  "packageWeight": -5,
                  "priority": "URGENT"
                }
              ]
            }
            """;
        
        
        mockMvc.perform(post("/dispatch/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.validationErrors").isMap());
        
        
        verify(dispatchService, never()).saveOrders(any());
    }
    
    @Test
    @DisplayName("POST /orders - Should validate latitude range")
    void testAcceptOrdersWithInvalidLatitude() throws Exception {
        
        OrderDTO invalidOrder = OrderDTO.builder()
            .orderId("ORD-001")
            .latitude(95.0)  
            .longitude(77.2090)
            .address("Test Address, Delhi, India")
            .packageWeight(5000)
            .priority("HIGH")
            .build();
        
        OrderRequestDTO request = OrderRequestDTO.builder()
            .orders(List.of(invalidOrder))
            .build();
        
        
        mockMvc.perform(post("/dispatch/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.validationErrors['orders[0].latitude']")
                .value(containsString("Latitude must be between -90 and 90")));
    }
    
    @Test
    @DisplayName("POST /orders - Should validate priority enum")
    void testAcceptOrdersWithInvalidPriority() throws Exception {
        
        String invalidJson = """
            {
              "orders": [
                {
                  "orderId": "ORD-001",
                  "latitude": 28.6139,
                  "longitude": 77.2090,
                  "address": "Test Address, Delhi, India",
                  "packageWeight": 5000,
                  "priority": "URGENT"
                }
              ]
            }
            """;
        
        
        mockMvc.perform(post("/dispatch/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.validationErrors['orders[0].priority']")
                .value(containsString("Priority must be HIGH, MEDIUM, or LOW")));
    }
    
 
    
    @Test
    @DisplayName("POST /vehicles - Should return 200 OK with valid request")
    void testAcceptVehiclesSuccess() throws Exception {
        // GIVEN: Valid vehicle request
        VehicleRequestDTO request = createValidVehicleRequest();
        
        // GIVEN: Service returns success response
        ApiResponse mockResponse = ApiResponse.success(
            "Successfully saved 2 vehicles (Total capacity: 25000 grams)"
        );
        when(dispatchService.saveVehicles(any(VehicleRequestDTO.class)))
            .thenReturn(mockResponse);
        
        // WHEN & THEN: POST request should succeed
        mockMvc.perform(post("/dispatch/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value(containsString("Successfully saved")))
            .andExpect(jsonPath("$.message").value(containsString("2 vehicles")));
        
        verify(dispatchService, times(1)).saveVehicles(any(VehicleRequestDTO.class));
    }
    
    @Test
    @DisplayName("POST /vehicles - Should return 400 for invalid capacity")
    void testAcceptVehiclesWithInvalidCapacity() throws Exception {
        // GIVEN: Vehicle with invalid capacity
        VehicleDTO invalidVehicle = VehicleDTO.builder()
            .vehicleId("VEH-001")
            .capacity(500)  // Invalid: below minimum (1000)
            .currentLatitude(28.6139)
            .currentLongitude(77.2090)
            .currentAddress("Test Location")
            .build();
        
        VehicleRequestDTO request = VehicleRequestDTO.builder()
            .vehicles(List.of(invalidVehicle))
            .build();
        
        // WHEN & THEN: Should fail validation
        mockMvc.perform(post("/dispatch/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.validationErrors['vehicles[0].capacity']")
                .value(containsString("Capacity must be at least 1000")));
    }
    
    
    @Test
    @DisplayName("GET /plan - Should return 200 OK with dispatch plan")
    void testGetDispatchPlanSuccess() throws Exception {
        
        DispatchPlanResponseDTO mockPlan = createMockDispatchPlan();
        when(dispatchService.getDispatchPlan()).thenReturn(mockPlan);
        
      
        mockMvc.perform(get("/dispatch/plan"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.message").value("Dispatch plan generated successfully"))
            .andExpect(jsonPath("$.dispatchPlan").isArray())
            .andExpect(jsonPath("$.dispatchPlan[0].vehicleId").value("VEH-001"))
            .andExpect(jsonPath("$.dispatchPlan[0].totalLoad").value(5000))
            .andExpect(jsonPath("$.dispatchPlan[0].assignedOrders").isArray())
            .andExpect(jsonPath("$.summary.totalOrders").value(2))
            .andExpect(jsonPath("$.summary.assignedOrders").value(2));
        
        verify(dispatchService, times(1)).getDispatchPlan();
    }
    
    @Test
    @DisplayName("GET /plan - Should return 400 when no orders exist")
    void testGetDispatchPlanWithNoOrders() throws Exception {
        // GIVEN: Service throws NoOrdersException
        when(dispatchService.getDispatchPlan())
            .thenThrow(new NoOrdersException());
        
        // WHEN & THEN: Should return 400 Bad Request
        mockMvc.perform(get("/dispatch/plan"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").value(containsString("No orders available")));
        
        verify(dispatchService, times(1)).getDispatchPlan();
    }
    
    @Test
    @DisplayName("GET /plan - Should return 400 when no vehicles exist")
    void testGetDispatchPlanWithNoVehicles() throws Exception {
        // GIVEN: Service throws NoVehiclesException
        when(dispatchService.getDispatchPlan())
            .thenThrow(new NoVehiclesException());
        
        // WHEN & THEN: Should return 400 Bad Request
        mockMvc.perform(get("/dispatch/plan"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value(containsString("No vehicles available")));
    }
    
   
    @Test
    @DisplayName("GET /health - Should return 200 OK")
    void testHealthCheck() throws Exception {
        // WHEN & THEN: Health check should always succeed
        mockMvc.perform(get("/dispatch/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Dispatch API is running"));
    }
    
  
    
    private OrderRequestDTO createValidOrderRequest() {
        OrderDTO order1 = OrderDTO.builder()
            .orderId("ORD-001")
            .latitude(28.6139)
            .longitude(77.2090)
            .address("Connaught Place, New Delhi, India")
            .packageWeight(5000)
            .priority("HIGH")
            .build();
        
        OrderDTO order2 = OrderDTO.builder()
            .orderId("ORD-002")
            .latitude(28.5355)
            .longitude(77.3910)
            .address("Sector 18, Noida, UP, India")
            .packageWeight(3000)
            .priority("MEDIUM")
            .build();
        
        return OrderRequestDTO.builder()
            .orders(List.of(order1, order2))
            .build();
    }
    
    private VehicleRequestDTO createValidVehicleRequest() {
        VehicleDTO vehicle1 = VehicleDTO.builder()
            .vehicleId("VEH-001")
            .capacity(10000)
            .currentLatitude(28.6139)
            .currentLongitude(77.2090)
            .currentAddress("Delhi Hub, Central Delhi")
            .build();

        VehicleDTO vehicle2 = VehicleDTO.builder()
            .vehicleId("VEH-002")
            .capacity(15000)
            .currentLatitude(28.5355)
            .currentLongitude(77.3910)
            .currentAddress("Noida Hub, Sector 18")
            .build();
        
        return VehicleRequestDTO.builder()
            .vehicles(List.of(vehicle1, vehicle2))
            .build();
    }
    
    private DispatchPlanResponseDTO createMockDispatchPlan() {
        AssignedOrderDTO assignedOrder = AssignedOrderDTO.builder()
            .orderId("ORD-001")
            .address("Test Address")
            .packageWeight(5000)
            .priority("HIGH")
            .distanceFromVehicle("4.20 km")
            .build();
        
        VehiclePlanDTO vehiclePlan = VehiclePlanDTO.builder()
            .vehicleId("VEH-001")
            .totalLoad(5000)
            .totalDistance("4.20 km")
            .assignedOrders(List.of(assignedOrder))
            .orderCount(1)
            .utilizationPercentage(50.0)
            .build();
        
        PlanSummaryDTO summary = PlanSummaryDTO.builder()
            .totalOrders(2)
            .assignedOrders(2)
            .unassignedOrders(0)
            .totalVehicles(2)
            .usedVehicles(1)
            .totalDistanceCovered("4.20 km")
            .averageUtilization(50.0)
            .build();
        
        return DispatchPlanResponseDTO.builder()
            .message("Dispatch plan generated successfully")
            .status("SUCCESS")
            .dispatchPlan(List.of(vehiclePlan))
            .summary(summary)
            .build();
    }
}