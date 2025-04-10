package dev.twalidaziz.cms.item;

import java.math.BigDecimal;

public record ItemListWithStatusRecord(Long id,
                                       String sku,
                                       String name,
                                       Integer quantity,
                                       Integer inUse,
                                       Integer size,
                                       BigDecimal unitPrice,
                                       boolean isInOrder) {
}
