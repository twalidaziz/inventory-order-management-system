package dev.twalidaziz.cms.customer;

import dev.twalidaziz.cms.order.Order;
import dev.twalidaziz.cms.order.OrderRepository;
import dev.twalidaziz.cms.util.NotFoundException;
import dev.twalidaziz.cms.util.ReferencedWarning;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;


@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    public CustomerService(final CustomerRepository customerRepository,
            final OrderRepository orderRepository) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
    }

    public Page<CustomerDTO> getCustomers(int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Customer> customers = customerRepository.findAll(pageable);

        List<CustomerDTO> customerDTOs = customers.getContent().stream()
                .map(customer -> mapToDTO(customer, new CustomerDTO()))
                .toList();

        return new PageImpl<>(customerDTOs, pageable, customers.getTotalElements());
    }

    public Page<CustomerDTO> getCustomersByName(String name, int page, int size, String sortBy,
                                                String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Customer> customers = customerRepository.findByNameContainingIgnoreCase(name, pageable);

        List<CustomerDTO> customerDTOs = customers.getContent().stream()
                .map(customer -> mapToDTO(customer, new CustomerDTO()))
                .toList();

        return new PageImpl<>(customerDTOs, pageable, customers.getTotalElements());
    }

    public CustomerDTO get(final Long id) {
        return customerRepository.findById(id)
                .map(customer -> mapToDTO(customer, new CustomerDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public Long create(final CustomerDTO customerDTO) {
        final Customer customer = new Customer();
        mapToEntity(customerDTO, customer);
        return customerRepository.save(customer).getId();
    }

    public void update(final Long id, final CustomerDTO customerDTO) {
        final Customer customer = customerRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mapToEntity(customerDTO, customer);
        customerRepository.save(customer);
    }

    public void delete(final Long id) {
        customerRepository.deleteById(id);
    }

    private CustomerDTO mapToDTO(final Customer customer, final CustomerDTO customerDTO) {
        customerDTO.setId(customer.getId());
        customerDTO.setName(customer.getName());
        customerDTO.setEmail(customer.getEmail());
        customerDTO.setPhone(customer.getPhone());
        customerDTO.setAddress(customer.getAddress());
        customerDTO.setLastUpdated(customer.getLastUpdated());
        return customerDTO;
    }

    private Customer mapToEntity(final CustomerDTO customerDTO, final Customer customer) {
        customer.setName(customerDTO.getName());
        customer.setEmail(customerDTO.getEmail());
        customer.setPhone(customerDTO.getPhone());
        customer.setAddress(customerDTO.getAddress());
        customer.setLastUpdated(customerDTO.getLastUpdated());
        return customer;
    }

    public ReferencedWarning getReferencedWarning(final Long id) {
        final ReferencedWarning referencedWarning = new ReferencedWarning();
        final Customer customer = customerRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        final Order customerOrder = orderRepository.findFirstByCustomer(customer);
        if (customerOrder != null) {
            referencedWarning.setKey("customer.order.customer.referenced");
            referencedWarning.addParam(customerOrder.getId());
            return referencedWarning;
        }
        return null;
    }

}
