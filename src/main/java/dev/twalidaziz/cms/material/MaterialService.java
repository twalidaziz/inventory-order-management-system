package dev.twalidaziz.cms.material;

import dev.twalidaziz.cms.item.ItemRepository;
import dev.twalidaziz.cms.supplier.Supplier;
import dev.twalidaziz.cms.supplier.SupplierRepository;
import dev.twalidaziz.cms.util.NotFoundException;
import jakarta.transaction.Transactional;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
@Transactional
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final SupplierRepository supplierRepository;
    private final ItemRepository itemRepository;

    public MaterialService(final MaterialRepository materialRepository,
            final SupplierRepository supplierRepository, final ItemRepository itemRepository) {
        this.materialRepository = materialRepository;
        this.supplierRepository = supplierRepository;
        this.itemRepository = itemRepository;
    }

    public List<MaterialDTO> findAll() {
        final List<Material> materials = materialRepository.findAll(Sort.by("id"));
        return materials.stream()
                .map(material -> mapToDTO(material, new MaterialDTO()))
                .toList();
    }

    public MaterialDTO get(final Long id) {
        return materialRepository.findById(id)
                .map(material -> mapToDTO(material, new MaterialDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public Long create(final MaterialDTO materialDTO) {
        final Material material = new Material();
        mapToEntity(materialDTO, material);
        return materialRepository.save(material).getId();
    }

    public void update(final Long id, final MaterialDTO materialDTO) {
        final Material material = materialRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mapToEntity(materialDTO, material);
        materialRepository.save(material);
    }

    public void delete(final Long id) {
        final Material material = materialRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        // remove many-to-many relations at owning side
        itemRepository.findAllByMaterials(material)
                .forEach(item -> item.getMaterials().remove(material));
        materialRepository.delete(material);
    }

    private MaterialDTO mapToDTO(final Material material, final MaterialDTO materialDTO) {
        materialDTO.setId(material.getId());
        materialDTO.setName(material.getName());
        materialDTO.setSize(material.getSize());
        materialDTO.setCost(material.getCost());
        materialDTO.setStock(material.getStock());
        materialDTO.setSupplier(material.getSupplier().stream()
                .map(supplier -> supplier.getId())
                .toList());
        return materialDTO;
    }

    private Material mapToEntity(final MaterialDTO materialDTO, final Material material) {
        material.setName(materialDTO.getName());
        material.setSize(materialDTO.getSize());
        material.setCost(materialDTO.getCost());
        material.setStock(materialDTO.getStock());
        final List<Supplier> supplier = supplierRepository.findAllById(
                materialDTO.getSupplier() == null ? Collections.emptyList() : materialDTO.getSupplier());
        if (supplier.size() != (materialDTO.getSupplier() == null ? 0 : materialDTO.getSupplier().size())) {
            throw new NotFoundException("one of supplier not found");
        }
        material.setSupplier(new HashSet<>(supplier));
        return material;
    }

}
