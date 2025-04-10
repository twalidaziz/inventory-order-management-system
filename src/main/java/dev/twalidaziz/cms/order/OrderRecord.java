package dev.twalidaziz.cms.order;

import java.time.OffsetDateTime;

public record OrderRecord(Long id,
                          String orderNumber,
                          String description,
                          String customerName,
                          OrderStatus status,
                          OffsetDateTime lastUpdated) {
}
