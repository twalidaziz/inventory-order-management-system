package dev.twalidaziz.cms.order;

import dev.twalidaziz.cms.customer.Customer;
import dev.twalidaziz.cms.item.Item;
import java.util.List;

import dev.twalidaziz.cms.order_item.OrderItemRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface OrderRepository extends JpaRepository<Order, Long> {

    @Override
    Page<Order> findAll(Pageable pageable);

    @Query(value =
            "SELECT new dev.twalidaziz.cms.order.OrderRecord(o.id, o.orderNumber , o.description, c.name, o.status, o.lastUpdated) " +
            "FROM Order o " +
            "JOIN Customer c ON o.customer.id = c.id",
            countQuery = "SELECT COUNT(*) FROM Order o JOIN Customer c ON o.customer.id = c.id")
    Page<OrderRecord> findAllOrdersWithCustomerName(Pageable pageable);

    @Query(value =
            "SELECT new dev.twalidaziz.cms.order.OrderEditFormRecord(o.id, o.orderNumber, o.description, o.customer.name, o.contactPerson, o.salesperson, o.totalCost, o.status, o.dateCreated, o.lastUpdated) " +
            "FROM Order o " +
            "WHERE o.id = :id")
    OrderEditFormRecord findOrderById(@Param("id") Long id);

    @Query("""
        SELECT new dev.twalidaziz.cms.order.OrderRecord(o.id, o.orderNumber, o.description, o.customer.name, o.status, o.lastUpdated)
        FROM Order o
        WHERE o.orderNumber LIKE :keyword OR o.description LIKE %:keyword%
    """)
    Page<OrderRecord> findOrderByOrderNumberOrDescription(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
        SELECT new dev.twalidaziz.cms.order_item.OrderItemRecord(oi.orderId, oi.itemId, oi.item.sku, oi.item.name, oi.quantity, oi.quantityDeficit, oi.quantityCompleted, oi.totalCost, oi.dateCreated)
        FROM OrderItem oi
        WHERE oi.orderId = :id
        ORDER BY oi.dateCreated
    """)
    List<OrderItemRecord> findOrderItemsByOrderId(@Param("id") Long id);

    //Order findFirstByItems(Item item);

    Order findFirstByCustomer(Customer customer);

    //List<Order> findAllByItems(Item item);

    boolean existsByOrderNumberIgnoreCase(String orderNumber);

    long countByStatus(@Param("status") OrderStatus status);
}
