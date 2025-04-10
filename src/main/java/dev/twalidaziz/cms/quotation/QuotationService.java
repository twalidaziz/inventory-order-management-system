package dev.twalidaziz.cms.quotation;

import dev.twalidaziz.cms.order.Order;
import dev.twalidaziz.cms.order.OrderRepository;
import dev.twalidaziz.cms.util.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
public class QuotationService {

    private final QuotationRepository quotationRepository;
    private final OrderRepository orderRepository;

    public QuotationService(QuotationRepository quotationRepository, OrderRepository orderRepository) {
        this.quotationRepository = quotationRepository;
        this.orderRepository = orderRepository;
    }

    public Quotation prepareQuotation(Long orderId) {

        Order order = orderRepository.findById(orderId).orElseThrow(() -> new NotFoundException("Order not found"));

        Quotation quotation = new Quotation();
        quotation.setFilename(OffsetDateTime.now().toString());
        quotation.setFile(null);
        quotation.setOrder(order);
        quotation.setDateCreated(OffsetDateTime.now());

        Quotation newQuotation = quotationRepository.save(quotation);

        String suffixNumber = String.format("%04d", newQuotation.getId());
        String newQuotationNumber = "Q" + orderId + suffixNumber;

        newQuotation.setQuotationNumber(newQuotationNumber);
        newQuotation.setFilename("order-quotation-" + orderId + suffixNumber);

        return quotationRepository.save(newQuotation);
    }

    public void saveQuotationData(Long quotationId, byte[] data) {

        Quotation quotation = quotationRepository.findById(quotationId)
                .orElseThrow(() -> new NotFoundException("Quotation not found"));

        quotation.setFile(data);
        quotationRepository.save(quotation);
    }

    public Page<QuotationListRecord> getQuotationByNumber(Long orderId, String keyword, int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        return quotationRepository.findByQuotationNumber(orderId, keyword, pageable);
    }

    public Page<Quotation> getQuotationByOrderId(Long orderId, int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        return quotationRepository.findAllByOrderId(orderId, pageable);
    }


    public Optional<Quotation> getQuotationById(Long id) {
        return quotationRepository.findById(id);
    }
}
