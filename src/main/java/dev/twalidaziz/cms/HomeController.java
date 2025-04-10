package dev.twalidaziz.cms;

import dev.twalidaziz.cms.customer.CustomerRepository;
import dev.twalidaziz.cms.item.ItemRepository;
import dev.twalidaziz.cms.order.OrderRepository;
import dev.twalidaziz.cms.order.OrderStatus;
import dev.twalidaziz.cms.supplier.SupplierRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class HomeController {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;

    public HomeController(OrderRepository orderRepository,
                          ItemRepository itemRepository,
                          CustomerRepository customerRepository,
                          SupplierRepository supplierRepository) {
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
        this.customerRepository = customerRepository;
        this.supplierRepository = supplierRepository;
    }

    @GetMapping("/")
    public String index(final Model model) {

        long totalOrders = orderRepository.count();
        long completedOrders = orderRepository.countByStatus(OrderStatus.COMPLETED);

        long totalItems = itemRepository.count();
        long lowStockItems = itemRepository.countLowStock(5);
        long noStockItems = itemRepository.countOutOfStock();

        long totalCustomers = customerRepository.count();
        long totalSuppliers = supplierRepository.count();

        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("openOrders", totalOrders - completedOrders);
        model.addAttribute("completedOrders", completedOrders);

        model.addAttribute("totalItems", totalItems);
        model.addAttribute("lowStockItems", lowStockItems);
        model.addAttribute("noStockItems", noStockItems);

        model.addAttribute("totalCustomers", totalCustomers);
        model.addAttribute("totalSuppliers", totalSuppliers);

        return "dashboard";
    }

    @GetMapping("/quot")
    public String quotation() {
        return "invoice";
    }

}
