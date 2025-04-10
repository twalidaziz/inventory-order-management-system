package dev.twalidaziz.cms.order;

import dev.twalidaziz.cms.HtmlRenderer;
import dev.twalidaziz.cms.PdfGenerator;
import dev.twalidaziz.cms.customer.Customer;
import dev.twalidaziz.cms.customer.CustomerRepository;
import dev.twalidaziz.cms.item.*;
import dev.twalidaziz.cms.order_item.OrderItemRecord;
import dev.twalidaziz.cms.quotation.Quotation;
import dev.twalidaziz.cms.quotation.QuotationListRecord;
import dev.twalidaziz.cms.quotation.QuotationService;
import dev.twalidaziz.cms.util.CustomCollectors;
import dev.twalidaziz.cms.util.NotFoundException;
import dev.twalidaziz.cms.util.WebUtils;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final ItemRepository itemRepository;
    private final CustomerRepository customerRepository;
    private final ItemService itemService;
    private final OrderRepository orderRepository;
    private final HtmlRenderer htmlRenderer;
    private final PdfGenerator pdfGenerator;
    private final QuotationService quotationService;

    public OrderController(final OrderService orderService, final ItemRepository itemRepository,
                           final CustomerRepository customerRepository, final ItemService itemService,
                           final OrderRepository orderRepository, HtmlRenderer htmlRenderer, PdfGenerator pdfGenerator, QuotationService quotationService) {
        this.orderService = orderService;
        this.itemRepository = itemRepository;
        this.customerRepository = customerRepository;
        this.itemService = itemService;
        this.orderRepository = orderRepository;
        this.htmlRenderer = htmlRenderer;
        this.pdfGenerator = pdfGenerator;
        this.quotationService = quotationService;
    }

    @ModelAttribute
    public void prepareContext(final Model model) {
        model.addAttribute("statusValues", OrderStatus.values());
        model.addAttribute("itemsValues", itemRepository.findAll(Sort.by("id"))
                .stream()
                .collect(CustomCollectors.toSortedMap(Item::getId, Item::getId)));
        model.addAttribute("customerValues", customerRepository.findAll(Sort.by("id"))
                .stream()
                .collect(CustomCollectors.toSortedMap(Customer::getId, Customer::getName)));
    }

    @GetMapping
    public String getOrdersPage(final Model model,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "5") int size,
                       @RequestParam(defaultValue = "lastUpdated") String sortBy,
                       @RequestParam(defaultValue = "DESC") String direction) {

        Page<OrderRecord> orders = orderService.getAllOrdersWithCustomerName(page, size, sortBy, direction);

        int totalOrders = (int) orders.getTotalElements();
        int startRecord = (page * size) + 1;
        int endRecord = Math.min(startRecord + size - 1, totalOrders);

        model.addAttribute("orders", orders.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orders.getTotalPages());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        model.addAttribute("totalOrders", orders.getTotalElements());
        model.addAttribute("startRecord", startRecord);
        model.addAttribute("endRecord", endRecord);

        return "order/list";
    }

    @GetMapping("/list")
    public String list(final Model model,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "5") int size,
                       @RequestParam(defaultValue = "lastUpdated") String sortBy,
                       @RequestParam(defaultValue = "DESC") String direction) {

        Page<OrderRecord> orders = orderService.getAllOrdersWithCustomerName(page, size, sortBy, direction);

        int totalOrders = (int) orders.getTotalElements();
        int startRecord = (page * size) + 1;
        int endRecord = Math.min(startRecord + size - 1, totalOrders);

        model.addAttribute("orders", orders.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orders.getTotalPages());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        model.addAttribute("totalOrders", orders.getTotalElements());
        model.addAttribute("startRecord", startRecord);
        model.addAttribute("endRecord", endRecord);

        return "order/list :: orderRows";
    }

    @GetMapping("/edit/{id}")
    public String getOrderEditForm(@PathVariable(name = "id") Long id, Model model) {

        OrderEditFormRecord order = orderService.getOrderWithCustomerName(id);
        model.addAttribute("order", order);
        model.addAttribute("statuses", OrderStatus.values());

        return "fragments/edit-order-card :: editOrderForm";
    }

    @GetMapping("/edit-details/{id}")
    public String getOrderDetailsEditForm(@PathVariable(name = "id") Long id, final Model model) {

        OrderEditFormRecord order = orderService.getOrderWithCustomerName(id);
        model.addAttribute("order", order);
        model.addAttribute("statuses", OrderStatus.values());

        return "order/fragments/edit :: mainContent";
    }

    @GetMapping("/edit-items/{id}")
    public String getOrderItemsEditForm(@PathVariable(name = "id") Long id, final Model model) {

        OrderEditFormRecord order = orderService.getOrderWithCustomerName(id);
        model.addAttribute("order", order);
        model.addAttribute("statuses", OrderStatus.values());

        return "order/fragments/items :: mainContent";
    }

    @PostMapping("/update/{id}")
    public String updateOrder(@PathVariable(name = "id") final Long id,
                              @ModelAttribute("order") @Valid final OrderEditFormRecord orderDTO,
                              final Model model) {

        orderService.update(id, orderDTO);
        OrderEditFormRecord order = orderService.getOrderWithCustomerName(id);

        model.addAttribute("order", order);
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("successMessage", "Order details updated successfully.");

        return "order/fragments/edit :: mainContent";
    }

    @GetMapping("/new")
    public String getNewOrderForm(Model model) {
        model.addAttribute("customers", customerRepository.findAllWithIdAndName());
        model.addAttribute("statuses", OrderStatus.values());
        return "fragments/new-order-card :: newOrderForm";
    }

    @PostMapping("/add")
    public String addOrder(@ModelAttribute("order") @Valid final OrderDTO orderDTO,
                              final Model model, final BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            return "fragments/new-order-card :: newOrderForm";
        }
        orderService.create(orderDTO);
        return list(model, 0, 5, "lastUpdated", "DESC");
    }

    @GetMapping("/search")
    public String searchOrders(final Model model,
                               @RequestParam(name = "keyword") String keyword,
                               @RequestParam(defaultValue ="0") int page,
                               @RequestParam(defaultValue = "5") int size,
                               @RequestParam(defaultValue = "lastUpdated") String sortBy,
                               @RequestParam(defaultValue = "DESC") String direction) {

        Page<OrderRecord> orders = orderService.getOrdersByOrderNumberOrDescription(
                keyword, page, size, sortBy, direction
        );

        long totalElements = orders.getTotalElements();

        if(totalElements == 0) {
            model.addAttribute("message", "Order not found");
            return "fragments/search-not-found :: searchNotFound";
        } else {
            model.addAttribute("orders", orders.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", orders.getTotalPages());
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("direction", direction);
            model.addAttribute("totalOrders", totalElements);
        }
        return "order/list :: orderRows";
    }
/**
    @GetMapping("/add")
    public String add(@ModelAttribute("order") final OrderDTO orderDTO) {
        return "order/add";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute("order") @Valid final OrderDTO orderDTO,
            final BindingResult bindingResult, final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "order/add";
        }
        orderService.create(orderDTO);
        redirectAttributes.addFlashAttribute(WebUtils.MSG_SUCCESS, WebUtils.getMessage("order.create.success"));
        return "redirect:/orders";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable(name = "id") final Long id, final Model model) {
        model.addAttribute("order", orderService.get(id));
        return "order/edit";
    }
**/
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable(name = "id") final Long id,
            final RedirectAttributes redirectAttributes) {
        orderService.delete(id);
        redirectAttributes.addFlashAttribute(WebUtils.MSG_INFO, WebUtils.getMessage("order.delete.success"));
        return "redirect:/orders";
    }

    @GetMapping("/items/{id}")
    public String getOrderItems(@PathVariable(name = "id") final Long id,
                                final Model model,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "7") int size,
                                @RequestParam(defaultValue = "id") String sortBy,
                                @RequestParam(defaultValue = "ASC") String direction) {

        OrderEditFormRecord order = orderService.getOrderWithCustomerName(id);
        List<OrderItemRecord> orderItems = orderService.getOrderItemsByOrderId(id);
        Page<ItemListRecord> items = itemService.getAllItems(page, size, sortBy, direction);

        // Extract item ids of order items
        List<Long> orderItemIds = orderItems.stream()
                .map(OrderItemRecord::itemId)
                .collect(Collectors.toList());

        List<ItemListWithStatusRecord> newItems = items.getContent().stream()
                .map(item -> new ItemListWithStatusRecord(
                        item.id(),
                        item.sku(),
                        item.name(),
                        item.quantity(),
                        item.inUse(),
                        item.size(),
                        item.unitPrice(),
                        orderItemIds.contains(item.id())))
                .collect(Collectors.toList());

        Page<ItemListWithStatusRecord> availableItems = new PageImpl<>(newItems, items.getPageable(), items.getTotalElements());

        int totalRecords = (int) availableItems.getTotalElements();
        int startRecord = (page * size) + 1;
        int endRecord = Math.min(startRecord + size - 1, totalRecords);

        model.addAttribute("order", order);
        model.addAttribute("orderItems", orderItems);
        model.addAttribute("totalOrderItems", orderItems.size());
        model.addAttribute("items", availableItems);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", availableItems.getTotalPages());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        model.addAttribute("totalRecords", totalRecords);
        model.addAttribute("startRecord", startRecord);
        model.addAttribute("endRecord", endRecord);

        return "order/fragments/items :: mainContent";
    }

    @PostMapping("/{orderId}/add-item")
    public String addOrderItem(@PathVariable(name = "orderId") Long orderId,
                               final Model model,
                               @RequestParam("itemId") Long itemId,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "7") int size,
                               @RequestParam(defaultValue = "id") String sortBy,
                               @RequestParam(defaultValue = "ASC") String direction) {

        Long addedItemId = orderService.addOrderItem(orderId, itemId);
        OrderEditFormRecord order = orderService.getOrderWithCustomerName(orderId);
        Page<ItemListRecord> items = itemService.getAllItems(page, size, sortBy, direction);
        List<OrderItemRecord> orderItems = orderService.getOrderItemsByOrderId(orderId);

        // Extract item ids of order items
        List<Long> orderItemIds = orderItems.stream()
                .map(OrderItemRecord::itemId)
                .collect(Collectors.toList());

        List<ItemListWithStatusRecord> newItems = items.getContent().stream()
                .map(item -> new ItemListWithStatusRecord(
                        item.id(),
                        item.sku(),
                        item.name(),
                        item.quantity(),
                        item.inUse(),
                        item.size(),
                        item.unitPrice(),
                        orderItemIds.contains(item.id())))
                .collect(Collectors.toList());

        Page<ItemListWithStatusRecord> availableItems = new PageImpl<>(newItems, items.getPageable(), items.getTotalElements());

        model.addAttribute("order", order);
        model.addAttribute("orderItems", orderItems);
        model.addAttribute("items", availableItems);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", availableItems.getTotalPages());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        model.addAttribute("totalItems", availableItems.getTotalElements());

        return "order/fragments/items :: mainContent";
    }

    @PostMapping("/{orderId}/delete-item")
    public String deleteOrderItem(@PathVariable(name = "orderId") Long orderId,
                               @RequestParam("itemId") Long itemId,
                               @RequestParam("quantity") int quantity,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "7") int size,
                               @RequestParam(defaultValue = "id") String sortBy,
                               @RequestParam(defaultValue = "ASC") String direction, final Model model) {

        orderService.deleteOrderItem(orderId, itemId, quantity);
        OrderEditFormRecord order = orderService.getOrderWithCustomerName(orderId);
        Page<ItemListRecord> items = itemService.getAllItems(page, size, sortBy, direction);
        List<OrderItemRecord> orderItems = orderService.getOrderItemsByOrderId(orderId);

        // Extract item ids of order items
        List<Long> orderItemIds = orderItems.stream()
                .map(OrderItemRecord::itemId)
                .collect(Collectors.toList());

        List<ItemListWithStatusRecord> newItems = items.getContent().stream()
                .map(item -> new ItemListWithStatusRecord(
                        item.id(),
                        item.sku(),
                        item.name(),
                        item.quantity(),
                        item.inUse(),
                        item.size(),
                        item.unitPrice(),
                        orderItemIds.contains(item.id())))
                .collect(Collectors.toList());

        Page<ItemListWithStatusRecord> availableItems = new PageImpl<>(newItems, items.getPageable(), items.getTotalElements());

        model.addAttribute("order", order);
        model.addAttribute("orderItems", orderItems);
        model.addAttribute("totalOrderItems", orderItems.size());
        model.addAttribute("items", availableItems);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", availableItems.getTotalPages());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        model.addAttribute("totalItems", availableItems.getTotalElements());

        return "order/fragments/items :: mainContent";
    }

    @PostMapping("/{orderId}/delete-all-items")
    public String deleteAllOrderItems(@PathVariable(name = "orderId") Long orderId,
                               final Model model,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "7") int size,
                               @RequestParam(defaultValue = "id") String sortBy,
                               @RequestParam(defaultValue = "ASC") String direction) {

        orderService.deleteAllOrderItems(orderId);
        OrderEditFormRecord order = orderService.getOrderWithCustomerName(orderId);
        Page<ItemListRecord> items = itemService.getAllItems(page, size, sortBy, direction);
        List<OrderItemRecord> orderItems = orderService.getOrderItemsByOrderId(orderId);

        List<ItemListWithStatusRecord> newItems = items.getContent().stream()
                .map(item -> new ItemListWithStatusRecord(
                        item.id(),
                        item.sku(),
                        item.name(),
                        item.quantity(),
                        item.inUse(),
                        item.size(),
                        item.unitPrice(),
                        false))
                .collect(Collectors.toList());

        Page<ItemListWithStatusRecord> availableItems = new PageImpl<>(newItems, items.getPageable(), items.getTotalElements());

        model.addAttribute("order", order);
        model.addAttribute("orderItems", orderItems);
        model.addAttribute("totalOrderItems", orderItems.size());
        model.addAttribute("items", availableItems);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", availableItems.getTotalPages());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        model.addAttribute("totalItems", availableItems.getTotalElements());
        model.addAttribute("successMessage", "All order item(s) have been removed.");

        return "order/fragments/items :: mainContent";
    }

    @PostMapping("/{orderId}/update-quantity")
    public String updateOrderItemQuantity(@PathVariable(name = "orderId") Long orderId,
                                          final Model model,
                                          @RequestParam("itemId") Long itemId,
                                          @RequestParam("quantity") Integer quantity,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "7") int size,
                                          @RequestParam(defaultValue = "id") String sortBy,
                                          @RequestParam(defaultValue = "ASC") String direction) {

        //orderService.updateOrderItemQuantity(orderId, itemId, quantity);
        List<OrderItemRecord> orderItems = orderService.updateOrderItemQuantity(orderId, itemId, quantity);
        Page<ItemListRecord> items = itemService.getAllItems(page, size, sortBy, direction);
        Long updatedItemId = itemId;

        // Extract item ids of order items
        List<Long> orderItemIds = orderItems.stream()
                .map(OrderItemRecord::itemId)
                .collect(Collectors.toList());

        List<ItemListWithStatusRecord> newItems = items.getContent().stream()
                .map(item -> new ItemListWithStatusRecord(
                        item.id(),
                        item.sku(),
                        item.name(),
                        item.quantity(),
                        item.inUse(),
                        item.size(),
                        item.unitPrice(),
                        orderItemIds.contains(item.id())))
                .collect(Collectors.toList());

        Page<ItemListWithStatusRecord> availableItems = new PageImpl<>(newItems, items.getPageable(), items.getTotalElements());

        model.addAttribute("order", orderRepository.findOrderById(orderId));
        model.addAttribute("orderItems", orderItems);
        model.addAttribute("totalOrderItems", orderItems.size());
        model.addAttribute("items", availableItems);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", availableItems.getTotalPages());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        model.addAttribute("totalItems", availableItems.getTotalElements());

        return "order/fragments/items :: mainContent";
    }

    @PostMapping("/{orderId}/di-quantity")
    public String incrementOrDecrementOrderItemQuantity(@PathVariable(name = "orderId") Long orderId,
                                          final Model model,
                                          @RequestParam("itemId") Long itemId,
                                          @RequestParam("quantity") Integer quantity,
                                          @RequestParam("action") String action,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "7") int size,
                                          @RequestParam(defaultValue = "id") String sortBy,
                                          @RequestParam(defaultValue = "ASC") String direction) {

        //orderService.updateOrderItemQuantity(orderId, itemId, quantity);

        // Increment or decrement the quantity based on the action
        if ("increment".equalsIgnoreCase(action)) {
            quantity++;
        } else if ("decrement".equalsIgnoreCase(action)) {
            quantity--;
        }
        List<OrderItemRecord> orderItems = orderService.updateOrderItemQuantity(orderId, itemId, quantity);
        Page<ItemListRecord> items = itemService.getAllItems(page, size, sortBy, direction);
        Long updatedItemId = itemId;

        // Extract item ids of order items
        List<Long> orderItemIds = orderItems.stream()
                .map(OrderItemRecord::itemId)
                .collect(Collectors.toList());

        List<ItemListWithStatusRecord> newItems = items.getContent().stream()
                .map(item -> new ItemListWithStatusRecord(
                        item.id(),
                        item.sku(),
                        item.name(),
                        item.quantity(),
                        item.inUse(),
                        item.size(),
                        item.unitPrice(),
                        orderItemIds.contains(item.id())))
                .collect(Collectors.toList());

        Page<ItemListWithStatusRecord> availableItems = new PageImpl<>(newItems, items.getPageable(), items.getTotalElements());

        model.addAttribute("order", orderRepository.findOrderById(orderId));
        model.addAttribute("orderItems", orderItems);
        model.addAttribute("totalOrderItems", orderItems.size());
        model.addAttribute("items", availableItems);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", availableItems.getTotalPages());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        model.addAttribute("totalItems", availableItems.getTotalElements());

        return "order/fragments/items :: mainContent";
    }

    @PostMapping("/{orderId}/deliverables/update")
    public String updateDeliverables(@PathVariable(name = "orderId") Long orderId,
                                     final Model model,
                                     @RequestParam("itemId") Long itemId) {

        orderService.updateOrderItemCompletedQuantity(orderId, itemId);
        OrderEditFormRecord order = orderService.getOrderWithCustomerName(orderId);
        List<OrderItemRecord> orderItems = orderService.getOrderItemsByOrderId(orderId);

        double totalCompleted = 0;
        double totalQuantity = 0;

        // Recalculate total quantity and total completed items
        for(OrderItemRecord orderItem : orderItems) {
            totalCompleted += orderItem.quantityCompleted();
            totalQuantity += orderItem.quantity();
        }

        double percentage = (totalCompleted / totalQuantity) * 100;
        double progress = Math.round(percentage * 100.0) / 100.0;

        model.addAttribute("order", order);
        model.addAttribute("orderItems", orderItems);
        model.addAttribute("totalOrderItems", orderItems.size());
        model.addAttribute("totalCompleted", (int) totalCompleted);
        model.addAttribute("totalQuantity", (int) totalQuantity);
        model.addAttribute("progress", progress);

        return "order/fragments/deliverables :: mainContent";
    }

    @GetMapping("/{orderId}/search-item")
    public String searchItem(@PathVariable(name = "orderId") Long orderId,
                             final Model model,
                             @RequestParam(name = "keyword") String keyword,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "7") int size,
                             @RequestParam(defaultValue = "id") String sortBy,
                             @RequestParam(defaultValue = "ASC") String direction) {

        OrderEditFormRecord order = orderService.getOrderWithCustomerName(orderId);
        List<OrderItemRecord> orderItems = orderService.getOrderItemsByOrderId(orderId);
        Page<ItemListRecord> items = itemService.getItemBySkuOrName(keyword, page, size, sortBy, direction);

        // Extract item ids of order items
        List<Long> orderItemIds = orderItems.stream()
                .map(OrderItemRecord::itemId)
                .collect(Collectors.toList());

        List<ItemListWithStatusRecord> newItems = items.getContent().stream()
                .map(item -> new ItemListWithStatusRecord(
                        item.id(),
                        item.sku(),
                        item.name(),
                        item.quantity(),
                        item.inUse(),
                        item.size(),
                        item.unitPrice(),
                        orderItemIds.contains(item.id())))
                .collect(Collectors.toList());

        Page<ItemListWithStatusRecord> availableItems = new PageImpl<>(newItems, items.getPageable(), items.getTotalElements());

        int totalRecords = (int) availableItems.getTotalElements();
        int startRecord = (page * size) + 1;
        int endRecord = Math.min(startRecord + size - 1, totalRecords);

        if(totalRecords == 0) {
            model.addAttribute("message", "Item not found");
            return "inventory/fragments/item-not-found :: mainContent";
        } else {
            model.addAttribute("order", order);
            model.addAttribute("items", availableItems);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", availableItems.getTotalPages());
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("direction", direction);
            model.addAttribute("totalRecords", totalRecords);
            model.addAttribute("startRecord", startRecord);
            model.addAttribute("endRecord", endRecord);
        }
        return "order/fragments/items :: itemList";
    }

    @GetMapping("/{orderId}/refresh-item-list")
    public String refreshItemList(@PathVariable(name = "orderId") Long orderId,
                                  final Model model,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "7") int size,
                                  @RequestParam(defaultValue = "id") String sortBy,
                                  @RequestParam(defaultValue = "ASC") String direction) {

        OrderEditFormRecord order = orderService.getOrderWithCustomerName(orderId);
        List<OrderItemRecord> orderItems = orderService.getOrderItemsByOrderId(orderId);
        Page<ItemListRecord> items = itemService.getAllItems(page, size, sortBy, direction);

        // Extract item ids of order items
        List<Long> orderItemIds = orderItems.stream()
                .map(OrderItemRecord::itemId)
                .collect(Collectors.toList());

        List<ItemListWithStatusRecord> newItems = items.getContent().stream()
                .map(item -> new ItemListWithStatusRecord(
                        item.id(),
                        item.sku(),
                        item.name(),
                        item.quantity(),
                        item.inUse(),
                        item.size(),
                        item.unitPrice(),
                        orderItemIds.contains(item.id())))
                .collect(Collectors.toList());

        Page<ItemListWithStatusRecord> availableItems = new PageImpl<>(newItems, items.getPageable(), items.getTotalElements());

        int totalRecords = (int) availableItems.getTotalElements();
        int startRecord = (page * size) + 1;
        int endRecord = Math.min(startRecord + size - 1, totalRecords);

        model.addAttribute("order", order);
        model.addAttribute("items", availableItems);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", availableItems.getTotalPages());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        model.addAttribute("totalRecords", totalRecords);
        model.addAttribute("startRecord", startRecord);
        model.addAttribute("endRecord", endRecord);

        return "order/fragments/items :: itemListContainer";
    }

    @PostMapping("/{orderId}/generate-quotation")
    public String generateQuotation(@PathVariable("orderId") Long orderId,
                                    final Model model,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size,
                                    @RequestParam(defaultValue = "dateCreated") String sortBy,
                                    @RequestParam(defaultValue = "DESC") String direction) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Quotation not found"));

        Customer customer = customerRepository.findById(order.getCustomer().getId())
                .orElseThrow(() -> new NotFoundException("Quotation not found"));

        List<OrderItemRecord> orderItems = orderService.getOrderItemsByOrderId(orderId);
        Quotation quotation  = quotationService.prepareQuotation(orderId);

        model.addAttribute("order", order);
        model.addAttribute("orderItems", orderItems);
        model.addAttribute("quotation", quotation);
        model.addAttribute("customer", customer);

        String htmlContent = htmlRenderer.renderTemplate("invoice", model);
        byte[] data = pdfGenerator.generatePdfFromHtml(htmlContent);

        quotationService.saveQuotationData(quotation.getId(), data);

        Page<Quotation> quotations = quotationService.getQuotationByOrderId(orderId, page, size, sortBy, direction);

        int totalRecords = (int) quotations.getTotalElements();
        int startRecord = (page * size) + 1;
        int endRecord = Math.min(startRecord + size - 1, totalRecords);

        model.addAttribute("quotations", quotations);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", quotations.getTotalPages());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        model.addAttribute("totalRecords", totalRecords);
        model.addAttribute("startRecord", startRecord);
        model.addAttribute("endRecord", endRecord);
        model.addAttribute("message", "Quotation " + quotation.getQuotationNumber() + " has been generated.");

        return "order/fragments/quotation-list :: mainContent";
    }

    @GetMapping("/{orderId}/quotations")
    public String listQuotations(@PathVariable("orderId") Long orderId,
                                 final Model model,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(defaultValue = "dateCreated") String sortBy,
                                 @RequestParam(defaultValue = "DESC") String direction) {

        Page<Quotation> quotations = quotationService.getQuotationByOrderId(orderId, page, size, sortBy, direction);
        OrderEditFormRecord order = orderService.getOrderWithCustomerName(orderId);

        int totalRecords = (int) quotations.getTotalElements();
        int startRecord = (page * size) + 1;
        int endRecord = Math.min(startRecord + size - 1, totalRecords);

        model.addAttribute("order", order);
        model.addAttribute("quotations", quotations);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", quotations.getTotalPages());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        model.addAttribute("totalRecords", totalRecords);
        model.addAttribute("startRecord", startRecord);
        model.addAttribute("endRecord", endRecord);


        return "order/fragments/quotation-list :: mainContent";
    }

    @GetMapping("/{orderId}/quotations/search")
    public String searchQuotation(@PathVariable("orderId") Long orderId,
                                  @RequestParam(name = "keyword") String keyword,
                                  @RequestParam(defaultValue ="0") int page,
                                  @RequestParam(defaultValue = "5") int size,
                                  @RequestParam(defaultValue = "lastUpdated") String sortBy,
                                  @RequestParam(defaultValue = "DESC") String direction,
                                  final Model model) {

        Page<QuotationListRecord> quotations = quotationService.getQuotationByNumber(
                orderId, keyword, page, size, sortBy, direction
        );

        long totalRecords = quotations.getTotalElements();

        if(totalRecords == 0) {
            model.addAttribute("message", "Quotation not found");
            return "fragments/search-not-found :: searchNotFound";
        } else {
            int startRecord = (page * size) + 1;
            int endRecord = (int) Math.min(startRecord + size - 1, totalRecords);
            OrderEditFormRecord order = orderService.getOrderWithCustomerName(orderId);

            model.addAttribute("order", order);
            model.addAttribute("quotations", quotations.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", quotations.getTotalPages());
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("direction", direction);
            model.addAttribute("totalRecords", totalRecords);
            model.addAttribute("startRecord", startRecord);
            model.addAttribute("endRecord", endRecord);
        }
        return "order/fragments/quotation-list :: quotationList";

    }

    @GetMapping("/{orderId}/quotations/download")
    public ResponseEntity<byte[]> downloadQuotation(@PathVariable("orderId") Long orderId,
                                                    @RequestParam("quotationId") Long quotationId) {

        return quotationService.getQuotationById(quotationId)
                .map(quotation -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_PDF);
                    headers.add("content-disposition", "inline;filename=" + quotation.getFilename());
                    //headers.setContentDispositionFormData("inline", quotation.getFilename());
                    return ResponseEntity.ok()
                            .headers(headers)
                            .body(quotation.getFile());
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("{orderId}/quotations/delete")
    public String deleteQuotation(@PathVariable("orderId") Long orderId,
                                  final Model model,
                                  @RequestParam("quotationId") Long quotationId,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(defaultValue = "dateCreated") String sortBy,
                                  @RequestParam(defaultValue = "DESC") String direction) {

        String quotationNumber = orderService.deleteOrderQuotation(quotationId);
        OrderEditFormRecord order = orderService.getOrderWithCustomerName(orderId);
        Page<Quotation> quotations = quotationService.getQuotationByOrderId(orderId, page, size, sortBy, direction);


        int totalRecords = (int) quotations.getTotalElements();
        int startRecord = (page * size) + 1;
        int endRecord = Math.min(startRecord + size - 1, totalRecords);

        model.addAttribute("order", order);
        model.addAttribute("quotations", quotations);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", quotations.getTotalPages());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        model.addAttribute("totalRecords", totalRecords);
        model.addAttribute("startRecord", startRecord);
        model.addAttribute("endRecord", endRecord);
        model.addAttribute("message", "Quotation " + quotationNumber + " has been deleted.");

        return "order/fragments/quotation-list :: mainContent";
    }

    @GetMapping("/{orderId}/deliverables")
    public String deliverables(@PathVariable("orderId") Long orderId, final Model model) {

        OrderEditFormRecord order = orderService.getOrderWithCustomerName(orderId);
        List<OrderItemRecord> orderItems = orderService.getOrderItemsByOrderId(orderId);

        double totalCompleted = 0;
        double totalQuantity = 0;

        // Recalculate total quantity and total completed items
        for(OrderItemRecord orderItem : orderItems) {
            totalCompleted += orderItem.quantityCompleted();
            totalQuantity += orderItem.quantity();
        }

        double percentage = (totalCompleted / totalQuantity) * 100;
        double progress = Math.round(percentage * 100.0) / 100.0;

        model.addAttribute("order", order);
        model.addAttribute("orderItems", orderItems);
        model.addAttribute("totalOrderItems", orderItems.size());
        model.addAttribute("totalCompleted", (int) totalCompleted);
        model.addAttribute("totalQuantity", (int) totalQuantity);
        model.addAttribute("progress", progress);

        return "order/fragments/deliverables :: mainContent";
    }

}
