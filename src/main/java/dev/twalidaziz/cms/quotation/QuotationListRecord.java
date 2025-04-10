package dev.twalidaziz.cms.quotation;

import java.time.OffsetDateTime;

public record QuotationListRecord(
        Long id,
        String quotationNumber,
        String filename,
        OffsetDateTime dateCreated) {
}
