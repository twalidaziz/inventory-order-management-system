package dev.twalidaziz.cms.supplier;

import org.springframework.data.jpa.repository.JpaRepository;


public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsBySsmNumberIgnoreCase(String ssmNumber);

}
