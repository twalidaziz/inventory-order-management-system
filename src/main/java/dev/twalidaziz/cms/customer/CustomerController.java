package dev.twalidaziz.cms.customer;

import dev.twalidaziz.cms.order.OrderDTO;
import dev.twalidaziz.cms.util.WebUtils;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerRepository customerRepository;

    public CustomerController(final CustomerService customerService,
                              final CustomerRepository customerRepository) {
        this.customerService = customerService;
        this.customerRepository = customerRepository;
    }

    @GetMapping
    public String getCustomersPage(final Model model,
                                   @RequestParam(defaultValue ="0") int page,
                                   @RequestParam(defaultValue = "5") int size,
                                   @RequestParam(defaultValue = "lastUpdated") String sortBy,
                                   @RequestParam(defaultValue = "DESC") String direction) {

        Page<CustomerDTO> customers = customerService.getCustomers(page, size, sortBy, direction);

        int totalRecords = (int) customers.getTotalElements();
        int startRecord = (page * size) + 1;
        int endRecord = Math.min(startRecord + size - 1, totalRecords);

        model.addAttribute("customers", customers.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", customers.getTotalPages());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        model.addAttribute("totalRecords", totalRecords);
        model.addAttribute("startRecord", startRecord);
        model.addAttribute("endRecord", endRecord);

        return "customer/list";
    }

    @GetMapping("/list")
    public String list(final Model model,
                       @RequestParam(defaultValue ="0") int page,
                       @RequestParam(defaultValue = "5") int size,
                       @RequestParam(defaultValue = "lastUpdated") String sortBy,
                       @RequestParam(defaultValue = "DESC") String direction) {

        Page<CustomerDTO> customers = customerService.getCustomers(page, size, sortBy, direction);

        int totalRecords = (int) customers.getTotalElements();
        int startRecord = (page * size) + 1;
        int endRecord = Math.min(startRecord + size - 1, totalRecords);

        model.addAttribute("customers", customers.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", customers.getTotalPages());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        model.addAttribute("totalRecords", totalRecords);
        model.addAttribute("startRecord", startRecord);
        model.addAttribute("endRecord", endRecord);

        return "customer/list :: customerRows";
    }

    @GetMapping("/edit/{id}")
    public String getCustomerEditForm(@PathVariable(name = "id") Long id, Model model) {

        CustomerDTO customer = customerService.get(id);
        model.addAttribute("customer", customer);

        return "fragments/edit-customer-card :: editCustomerForm";
    }

    @PostMapping("/update/{id}")
    public String updateCustomer(@PathVariable(name = "id") final Long id,
                                 @ModelAttribute("customer") @Valid final CustomerDTO customerDTO,
                                 final Model model,
                                 @RequestParam(defaultValue ="0") int page,
                                 @RequestParam(defaultValue = "5") int size,
                                 @RequestParam(defaultValue = "lastUpdated") String sortBy,
                                 @RequestParam(defaultValue = "DESC") String direction) {

        customerService.update(id, customerDTO);
        Page<CustomerDTO> customers = customerService.getCustomers(page, size, sortBy, direction);
        model.addAttribute("customers", customers.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", customers.getTotalPages());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        model.addAttribute("totalCustomers", customers.getTotalElements());
        return "customer/list :: customerRows";
    }

    @GetMapping("/new")
    public String getNewCustomerForm() {
        return "customer/fragments/new :: mainContent";
    }

    @PostMapping("/add")
    public String addCustomer(@ModelAttribute("customer") @Valid final CustomerDTO customerDTO,
                              final Model model, final BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            return "fragments/new-customer-card :: newCustomerForm";
        }
        customerService.create(customerDTO);
        return list(model, 0, 5, "lastUpdated", "DESC");
    }

    @GetMapping("/search")
    public String searchCustomers(final Model model,
                                  @RequestParam("name") String name,
                                  @RequestParam(defaultValue ="0") int page,
                                  @RequestParam(defaultValue = "5") int size,
                                  @RequestParam(defaultValue = "name") String sortBy,
                                  @RequestParam(defaultValue = "DESC") String direction) {

        Page<CustomerDTO> customers = customerService.getCustomersByName(name, page, size, sortBy, direction);

        int totalRecords = (int) customers.getTotalElements();
        int startRecord = (page * size) + 1;
        int endRecord = Math.min(startRecord + size - 1, totalRecords);

        model.addAttribute("customers", customers.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", customers.getTotalPages());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        model.addAttribute("totalRecords", totalRecords);
        model.addAttribute("startRecord", startRecord);
        model.addAttribute("endRecord", endRecord);

        return "customer/list :: customerRows";
    }
}
