package dev.twalidaziz.cms.order_item;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record OrderItemRecord(
        Long orderId,
        Long itemId,
        String sku,
        String name,
        Integer quantity,
        Integer quantityDeficit,
        Integer quantityCompleted,
        BigDecimal totalCost,
        OffsetDateTime dateCreated) {
}
