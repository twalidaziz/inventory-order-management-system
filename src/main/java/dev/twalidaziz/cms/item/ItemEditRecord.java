package dev.twalidaziz.cms.item;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ItemEditRecord(
        Long id,
        String sku,
        String name,
        Long density,
        Integer size,
        BigDecimal unitPrice,
        Integer quantity,
        String image,
        OffsetDateTime lastUpdated) {
}
