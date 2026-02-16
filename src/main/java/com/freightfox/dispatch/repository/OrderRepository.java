package com.freightfox.dispatch.repository;

import com.freightfox.dispatch.model.entity.DeliveryOrder;
import com.freightfox.dispatch.model.enums.Priority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * OrderRepository - Database access for DeliveryOrder entity
 * 
 * EXTENDS JpaRepository<DeliveryOrder, String>
 * - DeliveryOrder: The entity class (table to query)
 * - String: The type of the primary key (@Id field - orderId)
 * 
 * Think of it like:
 * const Order = mongoose.model('Order', orderSchema);
 * But Spring auto-generates ALL methods:
 * - save(), findById(), findAll(), deleteById(), etc.
 */

@Repository
// Marks this as a Spring-managed repository bean
// Spring will auto-create an implementation class at runtime
// Like: @Injectable() in NestJS

public interface OrderRepository extends JpaRepository<DeliveryOrder, String> {
    
    // =====================================================================
    // AUTO-PROVIDED METHODS (You get these for FREE from JpaRepository)
    // =====================================================================
    
    // DeliveryOrder save(DeliveryOrder order)
    //   → INSERT or UPDATE based on if ID exists
    //   → Like: Order.create() or order.save() in Mongoose
    
    // Optional<DeliveryOrder> findById(String orderId)
    //   → SELECT * FROM delivery_orders WHERE order_id = ?
    //   → Like: Order.findById(id)
    
    // List<DeliveryOrder> findAll()
    //   → SELECT * FROM delivery_orders
    //   → Like: Order.find()
    
    // void deleteById(String orderId)
    //   → DELETE FROM delivery_orders WHERE order_id = ?
    //   → Like: Order.findByIdAndDelete(id)
    
    // long count()
    //   → SELECT COUNT(*) FROM delivery_orders
    //   → Like: Order.countDocuments()
    
    // boolean existsById(String orderId)
    //   → SELECT COUNT(*) > 0 FROM delivery_orders WHERE order_id = ?
    //   → Like: Order.exists({ _id: id })
    
    // List<DeliveryOrder> saveAll(List<DeliveryOrder> orders)
    //   → Batch INSERT/UPDATE
    //   → Like: Order.insertMany()
    
    // void deleteAll()
    //   → DELETE FROM delivery_orders (DANGER!)
    
    
    // =====================================================================
    // CUSTOM QUERY METHODS (Spring auto-generates SQL from method names!)
    // =====================================================================
    
    /**
     * SPRING DATA JPA METHOD NAME QUERY
     * 
     * Method name pattern: findBy + FieldName
     * Spring parses the method name and generates SQL automatically!
     * 
     * Generated SQL:
     * SELECT * FROM delivery_orders WHERE priority = ?
     * 
     * Like Mongoose: Order.find({ priority: 'HIGH' })
     */
    List<DeliveryOrder> findByPriority(Priority priority);
    
    /**
     * Find all orders sorted by priority (HIGH → MEDIUM → LOW)
     * Then by createdAt (oldest first)
     * 
     * Method name pattern: findBy + OrderBy + Field + Direction
     * 
     * Generated SQL:
     * SELECT * FROM delivery_orders 
     * ORDER BY priority DESC, created_at ASC
     * 
     * Note: For Priority enum, Spring sorts by ordinal position
     * To sort HIGH→MEDIUM→LOW, you might need custom @Query
     */
    List<DeliveryOrder> findAllByOrderByCreatedAtAsc();
    
    /**
     * Custom query with multiple conditions
     * 
     * Pattern: findBy + Field1 + And + Field2
     * 
     * Generated SQL:
     * SELECT * FROM delivery_orders 
     * WHERE priority = ? AND package_weight <= ?
     */
    List<DeliveryOrder> findByPriorityAndPackageWeightLessThanEqual(
        Priority priority, 
        Integer maxWeight
    );
    
    
    // =====================================================================
    // @QUERY ANNOTATION (Manual JPQL - more control)
    // =====================================================================
    
    /**
     * JPQL Query (Java Persistence Query Language)
     * Similar to SQL but uses entity/field names instead of table/column names
     * 
     * JPQL: SELECT o FROM DeliveryOrder o
     * SQL:  SELECT * FROM delivery_orders
     * 
     * :priority is a named parameter (like $1, $2 in PostgreSQL)
     */
    @Query("SELECT o FROM DeliveryOrder o WHERE o.priority = :priority ORDER BY o.createdAt ASC")
    List<DeliveryOrder> findOrdersByPrioritySorted(@Param("priority") Priority priority);
    
    /**
     * Native SQL Query (when you need database-specific features)
     * 
     * nativeQuery = true → Use actual SQL instead of JPQL
     * Use when: complex joins, database functions, performance optimization
     */
    @Query(
        value = "SELECT * FROM delivery_orders WHERE priority = :priority ORDER BY created_at ASC",
        nativeQuery = true
    )
    List<DeliveryOrder> findOrdersByPriorityNative(@Param("priority") String priority);
    
    /**
     * Custom query for unassigned orders
     * (You'll add isAssigned field to entity later if needed)
     * 
     * For now, this finds all orders (placeholder for future logic)
     */
    @Query("SELECT o FROM DeliveryOrder o ORDER BY " +
           "CASE o.priority " +
           "  WHEN 'HIGH' THEN 1 " +
           "  WHEN 'MEDIUM' THEN 2 " +
           "  WHEN 'LOW' THEN 3 " +
           "END, " +
           "o.createdAt ASC")
    List<DeliveryOrder> findAllOrdersSortedByPriority();
    
    /**
     * Find orders within a weight range
     * 
     * Between query method
     */
    List<DeliveryOrder> findByPackageWeightBetween(Integer minWeight, Integer maxWeight);
    
    /**
     * Count orders by priority
     * 
     * Pattern: countBy + Field
     */
    long countByPriority(Priority priority);
    
    /**
     * Check if order exists by ID
     * (Already provided by JpaRepository, but showing custom variant)
     * 
     * Pattern: existsBy + Field
     */
    boolean existsByOrderId(String orderId);
}