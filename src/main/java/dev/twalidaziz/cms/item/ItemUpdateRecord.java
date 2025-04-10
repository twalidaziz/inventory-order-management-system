package dev.twalidaziz.cms.item;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ItemUpdateRecord(
        Long id,

        String sku,

        @NotBlank(message = "Name must be between 10-40 characters")
        @Size(min = 10, max = 40, message = "Name must be between 10-40 characters")
        String name,

        @NotNull(message = "Density is required")
        Long density,

        @Min(value = 1, message = "Size must be 1 or greater")
        Integer size,

        BigDecimal unitPrice,

        @Min(value = 0, message = "Stock must be 0 or greater")
        Integer quantity,

        MultipartFile image,

        OffsetDateTime lastUpdated) {
}
