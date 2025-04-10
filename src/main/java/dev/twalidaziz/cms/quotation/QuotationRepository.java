package dev.twalidaziz.cms.quotation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuotationRepository extends JpaRepository<Quotation, Long> {

    Page<Quotation> findAllByOrderId(@Param("orderId") Long orderId, Pageable pageable);

    @Query("""
        SELECT new dev.twalidaziz.cms.quotation.QuotationListRecord(q.id, q.quotationNumber, q.filename, q.dateCreated)
        FROM Quotation q
        WHERE (q.quotationNumber LIKE :keyword) AND q.order.id = :orderId
    """)
    Page<QuotationListRecord> findByQuotationNumber(@Param("orderId") Long orderId,
                                                    @Param("keyword") String keyword,
                                                    Pageable pageable);
}
