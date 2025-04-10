package dev.twalidaziz.cms.order;

import dev.twalidaziz.cms.customer.Customer;
import dev.twalidaziz.cms.customer.CustomerRepository;
import dev.twalidaziz.cms.item.Item;
import dev.twalidaziz.cms.item.ItemRepository;
import dev.twalidaziz.cms.order_item.OrderItem;
import dev.twalidaziz.cms.order_item.OrderItemRecord;
import dev.twalidaziz.cms.order_item.OrderItemRepository;
import dev.twalidaziz.cms.quotation.Quotation;
import dev.twalidaziz.cms.quotation.QuotationRepository;
import dev.twalidaziz.cms.util.NotFoundException;
import dev.twalidaziz.cms.util.ReferencedWarning;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final OrderItemRepository orderItemRepository;
    private final ItemRepository itemRepository;
    private final QuotationRepository quotationRepository;

    public OrderService(final OrderRepository orderRepository, final CustomerRepository customerRepository,
                        final OrderItemRepository orderItemRepository, final ItemRepository itemRepository,
                        final QuotationRepository quotationRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.orderItemRepository = orderItemRepository;
        this.itemRepository = itemRepository;
        this.quotationRepository = quotationRepository;
    }

    public Page<OrderDTO> getOrders(int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Order> orders = orderRepository.findAll(pageable);

        List<OrderDTO> orderDTOs = orders.getContent().stream()
                .map(order -> mapToDTO(order, new OrderDTO()))
                .toList();

        return new PageImpl<>(orderDTOs, pageable, orders.getTotalElements());
    }

    public Page<OrderRecord> getAllOrdersWithCustomerName(int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        return orderRepository.findAllOrdersWithCustomerName(pageable);
    }

    public OrderDTO get(final Long id) {
        return orderRepository.findById(id)
                .map(order -> mapToDTO(order, new OrderDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public OrderEditFormRecord getOrderWithCustomerName(final Long id) {
        return orderRepository.findOrderById(id);
    }

    public Long create(final OrderDTO orderDTO) {
        final Order order = new Order();
        mapToEntity(orderDTO, order);

        Order savedOrder = orderRepository.save(order);
        savedOrder.setOrderNumber(generateOrderNumber(savedOrder.getId()));

        return orderRepository.save(savedOrder).getId();
    }

    public void update(final Long id, final OrderEditFormRecord orderDTO) {
        final Order order = orderRepository.findById(id).orElseThrow(NotFoundException::new);
        order.setDescription(orderDTO.description().toUpperCase());
        order.setContactPerson(orderDTO.contactPerson().toUpperCase());
        order.setSalesperson(orderDTO.salesperson().toUpperCase());
        order.setStatus(orderDTO.status());

        //mapToEntity(orderDTO, order);
        orderRepository.save(order);
    }

    public void delete(final Long id) {
        orderRepository.deleteById(id);
    }

    public List<OrderItemRecord> getOrderItemsByOrderId(final Long orderId) {
        return orderRepository.findOrderItemsByOrderId(orderId);
    }

    private OrderDTO mapToDTO(final Order order, final OrderDTO orderDTO) {
        orderDTO.setId(order.getId());
        orderDTO.setOrderNumber(order.getOrderNumber());
        orderDTO.setDescription(order.getDescription());
        orderDTO.setTotalCost(order.getTotalCost());
        orderDTO.setSalesperson(order.getSalesperson());
        orderDTO.setContactPerson(order.getContactPerson());
        orderDTO.setStatus(order.getStatus());
        /**
        orderDTO.setItems(order.getItems().stream()
                .map(item -> item.getId())
                .toList());
         **/
        orderDTO.setCustomer(order.getCustomer() == null ? null : order.getCustomer().getId());
        orderDTO.setLastUpdated(order.getLastUpdated());
        return orderDTO;
    }

    private Order mapToEntity(final OrderDTO orderDTO, final Order order) {
        order.setOrderNumber("#");
        order.setDescription(orderDTO.getDescription().toUpperCase());
        order.setTotalCost(new BigDecimal("0.00"));
        order.setSalesperson(orderDTO.getSalesperson().toUpperCase());
        order.setContactPerson(orderDTO.getContactPerson().toUpperCase());
        order.setStatus(orderDTO.getStatus() != null ? orderDTO.getStatus() : OrderStatus.PENDING_APPROVAL);
        /**
        final List<Item> items = itemRepository.findAllById(
                orderDTO.getItems() == null ? Collections.emptyList() : orderDTO.getItems());
        if (items.size() != (orderDTO.getItems() == null ? 0 : orderDTO.getItems().size())) {
            throw new NotFoundException("one of items not found");
        }
        order.setItems(new HashSet<>(items));
         **/
        final Customer customer = orderDTO.getCustomer() == null ? null : customerRepository.findById(orderDTO.getCustomer())
                .orElseThrow(() -> new NotFoundException("customer not found"));
        order.setCustomer(customer);
        order.setLastUpdated(orderDTO.getLastUpdated());
        return order;
    }
/**
    public ReferencedWarning getReferencedWarning(final Long id) {
        final ReferencedWarning referencedWarning = new ReferencedWarning();
        final Order order = orderRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        final OrderItem orderOrderItem = orderItemRepository.findFirstByOrder(order);
        if (orderOrderItem != null) {
            referencedWarning.setKey("order.orderItem.order.referenced");
            referencedWarning.addParam(orderOrderItem.getId());
            return referencedWarning;
        }
        return null;
    }
 **/

    public Page<OrderRecord> getOrdersByOrderNumberOrDescription(String keyword, int page, int size,
                                                                 String sortBy, String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        return orderRepository.findOrderByOrderNumberOrDescription(keyword, pageable);
    }

    public Long addOrderItem(Long orderId, Long itemId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));

        int currentStock = item.getQuantity();
        int quantity = 1;
        int deficitQuantity = 0;
        int completedQuantity = quantity;

        if(quantity > currentStock) {
            deficitQuantity = Math.abs(currentStock - quantity);
            completedQuantity = currentStock;
            currentStock = 0;   // Set stock to 0 because order quantity exceeds stock quantity
        } else {
            currentStock = currentStock - quantity;
        }

        item.setQuantity(currentStock);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setOrderId(orderId);
        orderItem.setItemId(itemId);
        orderItem.setQuantity(quantity);
        orderItem.setQuantityDeficit(deficitQuantity);
        orderItem.setQuantityCompleted(completedQuantity);
        orderItem.setTotalCost(itemRepository.findUnitPriceById(itemId));
        orderItem.setDateCreated(OffsetDateTime.now());

        order.getOrderItems().add(orderItem);
        order.setTotalCost(order.calculateTotalCost());
        /**
        int newStockCount = item.getQuantity() - 1;
        // Set item stock count to 0 if order item quantity exceeds item stock count
        if(newStockCount < 0) {
            orderItem.setQuantityDeficit(Math.abs(newStockCount));
            item.setQuantity(0);
        } else {
            item.setQuantity(newStockCount);
        }
         **/

        itemRepository.save(item);
        orderRepository.save(order);

        return orderItem.getItemId();
    }

    public void deleteOrderItem(Long orderId, Long itemId, int quantity) {
        /**
        OrderItem orderItem = orderItemRepository.findByCompositeKey(orderId, itemId)
                .orElseThrow(() -> new NotFoundException("Order item not found"));
         **/

        Item item = itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("Order item not found"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        OrderItem orderItem = order.getOrderItems().stream()
                .filter(oi -> oi.getItem().getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Order item not found"));

        int restoredStock = item.getQuantity() + orderItem.getQuantity() - orderItem.getQuantityDeficit();
        item.setQuantity(restoredStock);

        /**
        item.setQuantity(item.getQuantity() + quantity);
         **/
        itemRepository.save(item);

        // Delete order item
        orderItemRepository.deleteByCompositeKey(orderId, itemId);

        // Recalculate order total cost
        order.setTotalCost(order.calculateTotalCost());
        orderRepository.save(order);
    }

    public void deleteAllOrderItems(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        // Restore item stock associated with order item
        for(OrderItem orderItem : order.getOrderItems()) {
            int quantity = orderItem.getQuantity();
            int deficitQuantity = orderItem.getQuantityDeficit();
            int currentStock = orderItem.getItem().getQuantity();

            // Calculate restored stock
            int restoredStock = currentStock + (quantity - deficitQuantity);

            // Set item stock to original value + completed quantity
            orderItem.getItem().setQuantity(restoredStock);
            itemRepository.save(orderItem.getItem());
        }
        orderItemRepository.deleteAllByOrderId(orderId);

        // Set order total cost to 0
        order.setTotalCost(BigDecimal.valueOf(0));
        orderRepository.save(order);
    }

    public void updateOrderItemCompletedQuantity(Long orderId, Long itemId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        // Find order item by item id
        OrderItem orderItem = order.getOrderItems().stream()
                .filter(oi -> oi.getItem().getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Order item not found"));

        // int newCompletedQuantity = completedQuantity + 1;
        // int newDeficitQuantity = deficitQuantity - newCompletedQuantity;

        orderItem.setQuantityCompleted(orderItem.getQuantityCompleted() + 1);
        orderItem.setQuantityDeficit(orderItem.getQuantityDeficit() - 1);

        orderItemRepository.save(orderItem);
    }

    public String deleteOrderQuotation(Long quotationId) {
        Quotation quotation = quotationRepository.findById(quotationId)
                .orElseThrow(() -> new NotFoundException("Quotation not found"));

        quotationRepository.deleteById(quotationId);
        return quotation.getQuotationNumber();
    }

    public List<OrderItemRecord> updateOrderItemQuantity(Long orderId, Long itemId, Integer quantity) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        // Find order item by item id
        OrderItem orderItem = order.getOrderItems().stream()
                .filter(oi -> oi.getItem().getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Order item not found"));

        Item item = orderItem.getItem();

        int currentStock = item.getQuantity();
        int currentQuantity = orderItem.getQuantity();
        int deficitQuantity = orderItem.getQuantityDeficit();

        // Restore stock
        int restoredStock = currentStock + currentQuantity - deficitQuantity;

        int newDeficitQuantity = 0;
        int completedQuantity = 0;

        if(quantity > restoredStock) {
            newDeficitQuantity = Math.abs(restoredStock - quantity);
            completedQuantity = restoredStock;
            restoredStock = 0;
        } else {
            newDeficitQuantity = 0;
            completedQuantity = quantity;
            restoredStock = restoredStock - quantity;
        }

        item.setQuantity(restoredStock);
        orderItem.setQuantity(quantity);
        orderItem.setQuantityDeficit(newDeficitQuantity);
        orderItem.setQuantityCompleted(completedQuantity);
        /**
        int newStockCount = (item.getQuantity() + orderItem.getQuantity()) - quantity;

        if(newStockCount < 0) {
            orderItem.setQuantityDeficit(orderItem.getQuantity());
            item.setQuantity(0);
        } else {
         //   TODO:
        }
        item.setQuantity((item.getQuantity() + orderItem.getQuantity()) - quantity);
         **/
        itemRepository.save(item);

        // Update order item quantity and total cost
        //orderItem.setQuantity(quantity);
        orderItem.setTotalCost(orderItem.calculateTotalCost());

        order.setTotalCost(order.calculateTotalCost());

        order.getOrderItems().size();

        orderRepository.save(order).getId();

        return order.getOrderItems().stream()
                .map(oi -> new OrderItemRecord(
                        oi.getOrderId(),
                        oi.getItemId(),
                        oi.getItem().getSku(),
                        oi.getItem().getName(),
                        oi.getQuantity(),
                        oi.getQuantityDeficit(),
                        oi.getQuantityCompleted(),
                        oi.getTotalCost(),
                        oi.getDateCreated()))
                .sorted(Comparator.comparing(OrderItemRecord::dateCreated))
                .collect(Collectors.toList());
    }

    public boolean orderNumberExists(final String orderNumber) {
        return orderRepository.existsByOrderNumberIgnoreCase(orderNumber);
    }

    private String generateOrderNumber(Long id) {
        return '#' + String.format("%04d", id);
    }

}
