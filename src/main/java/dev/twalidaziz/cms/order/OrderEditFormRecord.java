package dev.twalidaziz.cms.order;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record OrderEditFormRecord(Long id,
                                  String orderNumber,
                                  String description,
                                  String customerName,
                                  String contactPerson,
                                  String salesperson,
                                  BigDecimal totalCost,
                                  OrderStatus status,
                                  OffsetDateTime dateCreated,
                                  OffsetDateTime lastUpdated) {
}
