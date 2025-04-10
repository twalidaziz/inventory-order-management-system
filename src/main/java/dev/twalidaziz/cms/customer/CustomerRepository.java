package dev.twalidaziz.cms.customer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface CustomerRepository extends JpaRepository<Customer, Long> {
    @Override
    Page<Customer> findAll(Pageable pageable);

    @Query("""
    SELECT new dev.twalidaziz.cms.customer.CustomerWithIdAndNameRecord(c.id, c.name)
    FROM Customer c
    """)
    List<CustomerWithIdAndNameRecord> findAllWithIdAndName();

    Page<Customer> findByNameContainingIgnoreCase(String name, Pageable pageable);

}
