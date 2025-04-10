package dev.twalidaziz.cms.supplier;

import dev.twalidaziz.cms.material.MaterialRepository;
import dev.twalidaziz.cms.util.NotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
@Transactional
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final MaterialRepository materialRepository;

    public SupplierService(final SupplierRepository supplierRepository,
            final MaterialRepository materialRepository) {
        this.supplierRepository = supplierRepository;
        this.materialRepository = materialRepository;
    }

    public List<SupplierDTO> findAll() {
        final List<Supplier> suppliers = supplierRepository.findAll(Sort.by("id"));
        return suppliers.stream()
                .map(supplier -> mapToDTO(supplier, new SupplierDTO()))
                .toList();
    }

    public SupplierDTO get(final Long id) {
        return supplierRepository.findById(id)
                .map(supplier -> mapToDTO(supplier, new SupplierDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public Long create(final SupplierDTO supplierDTO) {
        final Supplier supplier = new Supplier();
        mapToEntity(supplierDTO, supplier);
        return supplierRepository.save(supplier).getId();
    }

    public void update(final Long id, final SupplierDTO supplierDTO) {
        final Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mapToEntity(supplierDTO, supplier);
        supplierRepository.save(supplier);
    }

    public void delete(final Long id) {
        final Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        // remove many-to-many relations at owning side
        materialRepository.findAllBySupplier(supplier)
                .forEach(material -> material.getSupplier().remove(supplier));
        supplierRepository.delete(supplier);
    }

    private SupplierDTO mapToDTO(final Supplier supplier, final SupplierDTO supplierDTO) {
        supplierDTO.setId(supplier.getId());
        supplierDTO.setName(supplier.getName());
        supplierDTO.setSsmNumber(supplier.getSsmNumber());
        supplierDTO.setContactPerson(supplier.getContactPerson());
        supplierDTO.setEmail(supplier.getEmail());
        supplierDTO.setPhone(supplier.getPhone());
        supplierDTO.setAddress(supplier.getAddress());
        return supplierDTO;
    }

    private Supplier mapToEntity(final SupplierDTO supplierDTO, final Supplier supplier) {
        supplier.setName(supplierDTO.getName());
        supplier.setSsmNumber(supplierDTO.getSsmNumber());
        supplier.setContactPerson(supplierDTO.getContactPerson());
        supplier.setEmail(supplierDTO.getEmail());
        supplier.setPhone(supplierDTO.getPhone());
        supplier.setAddress(supplierDTO.getAddress());
        return supplier;
    }

    public boolean nameExists(final String name) {
        return supplierRepository.existsByNameIgnoreCase(name);
    }

    public boolean ssmNumberExists(final String ssmNumber) {
        return supplierRepository.existsBySsmNumberIgnoreCase(ssmNumber);
    }

}
