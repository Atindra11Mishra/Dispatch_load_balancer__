package com.freightfox.dispatch.service;

import com.freightfox.dispatch.exception.*;
import com.freightfox.dispatch.model.dto.*;
import com.freightfox.dispatch.model.entity.DeliveryOrder;
import com.freightfox.dispatch.model.entity.Vehicle;
import com.freightfox.dispatch.model.enums.Priority;
import com.freightfox.dispatch.repository.OrderRepository;
import com.freightfox.dispatch.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 
 * 
 
 * 
 * @Mock:
 
 * @InjectMocks:
 
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DispatchService - Business Logic Tests")
class DispatchServiceTest {
    
   
    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private VehicleRepository vehicleRepository;
    
    @Mock
    private OptimizationEngine optimizationEngine;
   
    @InjectMocks
    private DispatchService dispatchService;
    
   
    private OrderRequestDTO validOrderRequest;
    private VehicleRequestDTO validVehicleRequest;
    private List<DeliveryOrder> mockOrders;
    private List<Vehicle> mockVehicles;
    
    @BeforeEach
    void setUp() {
        // GIVEN: Setup test data before each test
        validOrderRequest = createValidOrderRequest();
        validVehicleRequest = createValidVehicleRequest();
        mockOrders = createMockOrders();
        mockVehicles = createMockVehicles();
    }
    
   
    
    @Test
    @DisplayName("Should save orders successfully")
    void testSaveOrdersSuccess() {
    
        OrderRequestDTO request = validOrderRequest;
        
    
        when(orderRepository.existsById(anyString())).thenReturn(false);
        
      
        when(orderRepository.saveAll(anyList())).thenReturn(mockOrders);
        
      
        ApiResponse response = dispatchService.saveOrders(request);
        
       
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).contains("Successfully saved");
        assertThat(response.getMessage()).contains("2 orders");
        
       
        verify(orderRepository, times(2)).existsById(anyString());
        verify(orderRepository, times(1)).saveAll(anyList());
    }
    
    @Test
    @DisplayName("Should throw DuplicateOrderException when order ID already exists")
    void testSaveOrdersWithDuplicateId() {
    
        OrderRequestDTO request = validOrderRequest;
        
    
        when(orderRepository.existsById("ORD-001")).thenReturn(true);
        
       
        assertThatThrownBy(() -> dispatchService.saveOrders(request))
            .isInstanceOf(DuplicateOrderException.class)
            .hasMessageContaining("ORD-001")
            .hasMessageContaining("already exists");
        
       
        verify(orderRepository, never()).saveAll(anyList());
    }
    
    @Test
    @DisplayName("Should throw IllegalArgumentException for duplicate IDs in request")
    void testSaveOrdersWithDuplicatesInRequest() {
    
        OrderDTO order1 = createOrderDTO("ORD-001", 28.6, 77.2, 5000, "HIGH");
        OrderDTO order2 = createOrderDTO("ORD-001", 28.7, 77.3, 3000, "MEDIUM");  
        
        OrderRequestDTO request = OrderRequestDTO.builder()
            .orders(List.of(order1, order2))
            .build();
        
       
        assertThatThrownBy(() -> dispatchService.saveOrders(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("duplicate");
        
       
        verify(orderRepository, never()).existsById(anyString());
        verify(orderRepository, never()).saveAll(anyList());
    }
    
    @Test
    @DisplayName("Should rollback transaction if save fails")
    void testSaveOrdersTransactionRollback() {
    
        OrderRequestDTO request = validOrderRequest;
        
    
        when(orderRepository.existsById(anyString())).thenReturn(false);
        
        
        when(orderRepository.saveAll(anyList()))
            .thenThrow(new RuntimeException("Database error"));
        
       
        assertThatThrownBy(() -> dispatchService.saveOrders(request))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Database error");
        
       
    }
    
    
    
    @Test
    @DisplayName("Should save vehicles successfully")
    void testSaveVehiclesSuccess() {
    
        VehicleRequestDTO request = validVehicleRequest;
        
    
        when(vehicleRepository.existsById(anyString())).thenReturn(false);
        
       
        when(vehicleRepository.saveAll(anyList())).thenReturn(mockVehicles);
        
       
        ApiResponse response = dispatchService.saveVehicles(request);
        
        
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).contains("Successfully saved");
        assertThat(response.getMessage()).contains("2 vehicles");
        assertThat(response.getMessage()).contains("Total capacity");
        
        
        verify(vehicleRepository, times(2)).existsById(anyString());
        verify(vehicleRepository, times(1)).saveAll(anyList());
    }
    
    @Test
    @DisplayName("Should throw DuplicateVehicleException when vehicle ID already exists")
    void testSaveVehiclesWithDuplicateId() {
    
        VehicleRequestDTO request = validVehicleRequest;
        
    
        when(vehicleRepository.existsById("VEH-001")).thenReturn(true);
        
       
        assertThatThrownBy(() -> dispatchService.saveVehicles(request))
            .isInstanceOf(DuplicateVehicleException.class)
            .hasMessageContaining("VEH-001")
            .hasMessageContaining("already exists");
        
        
        verify(vehicleRepository, never()).saveAll(anyList());
    }
    
    
    @Test
    @DisplayName("Should generate dispatch plan successfully")
    void testGetDispatchPlanSuccess() {
        
        when(orderRepository.findAll()).thenReturn(mockOrders);
        when(vehicleRepository.findAll()).thenReturn(mockVehicles);
        
       
        DispatchPlanResponseDTO mockPlan = createMockDispatchPlan();
        when(optimizationEngine.optimizeDispatch(anyList(), anyList()))
            .thenReturn(mockPlan);
        
       
        DispatchPlanResponseDTO plan = dispatchService.getDispatchPlan();
        
        
        assertThat(plan).isNotNull();
        assertThat(plan.getStatus()).isEqualTo("SUCCESS");
        assertThat(plan.getDispatchPlan()).isNotEmpty();
        
        
        verify(orderRepository, times(1)).findAll();
        verify(vehicleRepository, times(1)).findAll();
        verify(optimizationEngine, times(1)).optimizeDispatch(anyList(), anyList());
    }
    
    @Test
    @DisplayName("Should throw NoOrdersException when no orders exist")
    void testGetDispatchPlanWithNoOrders() {
        
        when(orderRepository.findAll()).thenReturn(new ArrayList<>());
        
        
        assertThatThrownBy(() -> dispatchService.getDispatchPlan())
            .isInstanceOf(NoOrdersException.class)
            .hasMessageContaining("No orders available");
        
        
        verify(vehicleRepository, never()).findAll();
        verify(optimizationEngine, never()).optimizeDispatch(anyList(), anyList());
    }
    
    @Test
    @DisplayName("Should throw NoVehiclesException when no vehicles exist")
    void testGetDispatchPlanWithNoVehicles() {
        
        when(orderRepository.findAll()).thenReturn(mockOrders);
        when(vehicleRepository.findAll()).thenReturn(new ArrayList<>());
        
        
        assertThatThrownBy(() -> dispatchService.getDispatchPlan())
            .isInstanceOf(NoVehiclesException.class)
            .hasMessageContaining("No vehicles available");
        
        
        verify(optimizationEngine, never()).optimizeDispatch(anyList(), anyList());
    }
    
    @Test
    @DisplayName("Should call optimization engine with fetched data")
    void testGetDispatchPlanCallsOptimizationEngine() {
        
        when(orderRepository.findAll()).thenReturn(mockOrders);
        when(vehicleRepository.findAll()).thenReturn(mockVehicles);
        
        
        DispatchPlanResponseDTO mockPlan = createMockDispatchPlan();
        when(optimizationEngine.optimizeDispatch(mockOrders, mockVehicles))
            .thenReturn(mockPlan);
        
        
        DispatchPlanResponseDTO plan = dispatchService.getDispatchPlan();
        
        
        verify(optimizationEngine).optimizeDispatch(
            argThat(orders -> orders.size() == 2),
            argThat(vehicles -> vehicles.size() == 2)
        );
        
        assertThat(plan).isSameAs(mockPlan);
    }
    
    
    private OrderRequestDTO createValidOrderRequest() {
        OrderDTO order1 = createOrderDTO("ORD-001", 28.6139, 77.2090, 5000, "HIGH");
        OrderDTO order2 = createOrderDTO("ORD-002", 28.5355, 77.3910, 3000, "MEDIUM");
        
        return OrderRequestDTO.builder()
            .orders(List.of(order1, order2))
            .build();
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
    
    private VehicleRequestDTO createValidVehicleRequest() {
        VehicleDTO vehicle1 = createVehicleDTO("VEH-001", 28.6139, 77.2090, 10000);
        VehicleDTO vehicle2 = createVehicleDTO("VEH-002", 28.5355, 77.3910, 15000);
        
        return VehicleRequestDTO.builder()
            .vehicles(List.of(vehicle1, vehicle2))
            .build();
    }
    
    private VehicleDTO createVehicleDTO(String vehicleId, double lat, double lon, int capacity) {
        return VehicleDTO.builder()
            .vehicleId(vehicleId)
            .currentLatitude(lat)
            .currentLongitude(lon)
            .currentAddress("Test Location, Delhi, India")
            .capacity(capacity)
            .build();
    }
    
    private List<DeliveryOrder> createMockOrders() {
        DeliveryOrder order1 = DeliveryOrder.builder()
            .orderId("ORD-001")
            .latitude(28.6139)
            .longitude(77.2090)
            .address("Test Address 1")
            .packageWeight(5000)
            .priority(Priority.HIGH)
            .build();
        
        DeliveryOrder order2 = DeliveryOrder.builder()
            .orderId("ORD-002")
            .latitude(28.5355)
            .longitude(77.3910)
            .address("Test Address 2")
            .packageWeight(3000)
            .priority(Priority.MEDIUM)
            .build();
        
        return List.of(order1, order2);
    }
    
    private List<Vehicle> createMockVehicles() {
        Vehicle vehicle1 = Vehicle.builder()
            .vehicleId("VEH-001")
            .currentLatitude(28.6139)
            .currentLongitude(77.2090)
            .currentAddress("Test Location 1")
            .capacity(10000)
            .build();
        
        Vehicle vehicle2 = Vehicle.builder()
            .vehicleId("VEH-002")
            .currentLatitude(28.5355)
            .currentLongitude(77.3910)
            .currentAddress("Test Location 2")
            .capacity(15000)
            .build();
        
        return List.of(vehicle1, vehicle2);
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