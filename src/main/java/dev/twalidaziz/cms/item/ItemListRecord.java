package dev.twalidaziz.cms.item;

import dev.twalidaziz.cms.item_type.ItemType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ItemListRecord(Long id,
                             String sku,
                             String name,
                             Integer quantity,
                             Integer inUse,
                             Integer size,
                             BigDecimal unitPrice,
                             OffsetDateTime lastUpdated) {
}
