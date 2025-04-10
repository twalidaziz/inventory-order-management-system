package dev.twalidaziz.cms.material;

import dev.twalidaziz.cms.supplier.Supplier;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;


public interface MaterialRepository extends JpaRepository<Material, Long> {

    Material findFirstBySupplier(Supplier supplier);

    List<Material> findAllBySupplier(Supplier supplier);

}
