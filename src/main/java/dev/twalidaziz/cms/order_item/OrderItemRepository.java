package dev.twalidaziz.cms.order_item;

import dev.twalidaziz.cms.item.Item;
import dev.twalidaziz.cms.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    OrderItem findFirstByOrder(Order order);

    OrderItem findFirstByItem(Item item);

    List<OrderItem> findAllByOrderId(Long orderId);

    @Query("""
        SELECT OrderItem
        FROM OrderItem oi
        WHERE oi.orderId = :orderId AND oi.itemId = :itemId
    """)
    Optional<OrderItem> findByCompositeKey(@Param("orderId") Long orderId, @Param("itemId") Long itemId);

    @Modifying
    @Query("""
        DELETE FROM OrderItem oi
        WHERE oi.orderId = :orderId AND oi.itemId = :itemId
    """)
    void deleteByCompositeKey(@Param("orderId") Long orderId, @Param("itemId") Long itemId);

    @Modifying
    @Query("""
        DELETE FROM OrderItem oi
        WHERE oi.orderId = :orderId
    """)
    void deleteAllByOrderId(@Param("orderId") Long orderId);

}
