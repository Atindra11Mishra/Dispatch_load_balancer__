package com.freightfox.dispatch.repository;

import com.freightfox.dispatch.model.entity.DeliveryOrder;
import com.freightfox.dispatch.model.enums.Priority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;



@Repository


public interface OrderRepository extends JpaRepository<DeliveryOrder, String> {
    
    
    List<DeliveryOrder> findByPriority(Priority priority);
    
   
    List<DeliveryOrder> findAllByOrderByCreatedAtAsc();
    
   
    List<DeliveryOrder> findByPriorityAndPackageWeightLessThanEqual(
        Priority priority, 
        Integer maxWeight
    );
    
    
   
    @Query("SELECT o FROM DeliveryOrder o WHERE o.priority = :priority ORDER BY o.createdAt ASC")
    List<DeliveryOrder> findOrdersByPrioritySorted(@Param("priority") Priority priority);
    
    
    @Query(
        value = "SELECT * FROM delivery_orders WHERE priority = :priority ORDER BY created_at ASC",
        nativeQuery = true
    )
    List<DeliveryOrder> findOrdersByPriorityNative(@Param("priority") String priority);
    
    
    @Query("SELECT o FROM DeliveryOrder o ORDER BY " +
           "CASE o.priority " +
           "  WHEN 'HIGH' THEN 1 " +
           "  WHEN 'MEDIUM' THEN 2 " +
           "  WHEN 'LOW' THEN 3 " +
           "END, " +
           "o.createdAt ASC")
    List<DeliveryOrder> findAllOrdersSortedByPriority();
    
    
    List<DeliveryOrder> findByPackageWeightBetween(Integer minWeight, Integer maxWeight);
    
   
    long countByPriority(Priority priority);
    
    
    boolean existsByOrderId(String orderId);
}