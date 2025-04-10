package dev.twalidaziz.cms.item;

import dev.twalidaziz.cms.item_type.ItemType;
import dev.twalidaziz.cms.item_type.ItemTypeRepository;
import dev.twalidaziz.cms.material.Material;
import dev.twalidaziz.cms.material.MaterialRepository;
import dev.twalidaziz.cms.order.OrderRecord;
import dev.twalidaziz.cms.order.OrderRepository;
import dev.twalidaziz.cms.util.NotFoundException;
import dev.twalidaziz.cms.wood_density.WoodDensity;
import dev.twalidaziz.cms.wood_density.WoodDensityRepository;
import dev.twalidaziz.cms.wood_density.WoodDensityService;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
@Transactional
public class ItemService {

    private final ItemRepository itemRepository;
    private final MaterialRepository materialRepository;
    private final ItemTypeRepository itemTypeRepository;
    private final OrderRepository orderRepository;
    private final WoodDensityService woodDensityService;
    private final WoodDensityRepository woodDensityRepository;

    public ItemService(final ItemRepository itemRepository,
                       final MaterialRepository materialRepository,
                       final ItemTypeRepository itemTypeRepository,
                       final OrderRepository orderRepository,
                       final WoodDensityService woodDensityService, WoodDensityRepository woodDensityRepository) {
        this.itemRepository = itemRepository;
        this.materialRepository = materialRepository;
        this.itemTypeRepository = itemTypeRepository;
        this.orderRepository = orderRepository;
        this.woodDensityService = woodDensityService;
        this.woodDensityRepository = woodDensityRepository;
    }

    public Page<ItemListRecord> getAllItems(int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        return itemRepository.findAllItems(pageable);
    }

    public Page<ItemListRecord> getItemsByCategory(ItemCategory category, int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        return itemRepository.findItemsByCategory(category, pageable);
    }

    public List<ItemDTO> findAll() {
        final List<Item> items = itemRepository.findAll(Sort.by("id"));
        return items.stream()
                .map(item -> mapToDTO(item, new ItemDTO()))
                .toList();
    }

    public ItemDTO get(final Long id) {
        return itemRepository.findById(id)
                .map(item -> mapToDTO(item, new ItemDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public ItemEditRecord getItemById(final Long id) {
        Item item = itemRepository.findById(id).orElseThrow(() -> new NotFoundException("Item not found"));

        String itemImage = item.getImage() != null
                ? Base64.getEncoder().encodeToString(item.getImage())
                : null;

        return new ItemEditRecord(
                item.getId(),
                item.getSku(),
                item.getName(),
                item.getDensity().getId(),
                item.getSize(),
                item.getUnitPrice(),
                item.getQuantity(),
                itemImage,
                item.getLastUpdated());
    }

    public Page<ItemListRecord> getItemBySkuOrName(String keyword, int page, int size,
                                                                 String sortBy, String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        return itemRepository.findBySkuOrName(keyword, pageable);
    }

    public Long create(final ItemCreateRecord itemDTO, byte[] image) {
        final Item item = new Item();
        mapToEntity(itemDTO, item, image);
        return itemRepository.save(item).getId();
    }

    public void update(final Long id, final ItemUpdateRecord itemDTO, byte[] image) {
        final Item item = itemRepository.findById(id).orElseThrow(() -> new NotFoundException("Item not found"));
        item.setName(itemDTO.name().toUpperCase());

        final WoodDensity density = woodDensityRepository.findById(itemDTO.density())
                .orElseThrow(() -> new NotFoundException("Density not found"));
        item.setDensity(density);

        item.setSize(itemDTO.size());
        item.setSku("AP-" + woodDensityRepository.findCodeById(itemDTO.density()) + "-" + itemDTO.size());
        item.setUnitPrice(itemDTO.unitPrice());
        item.setQuantity(itemDTO.quantity());

        if(image != null) {
            item.setImage(image);
        }
        //mapToEntity(itemDTO, item);
        itemRepository.save(item);
    }
/**
    public void delete(final Long id) {
        final Item item = itemRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        // remove many-to-many relations at owning side
        orderRepository.findAllByItems(item)
                .forEach(order -> order.getOrderItems().remove(item));
        itemRepository.delete(item);
    }
**/

    public BigDecimal calculatePrice(Long densityId, Integer size) {
        BigDecimal squareFootagePrice = woodDensityService.getSquareFootagePriceById(densityId);
        return squareFootagePrice.multiply(BigDecimal.valueOf(size));
    }

    private ItemDTO mapToDTO(final Item item, final ItemDTO itemDTO) {
        itemDTO.setId(item.getId());
        itemDTO.setSku(item.getSku());
        itemDTO.setName(item.getName());
        itemDTO.setSize(item.getSize());
        itemDTO.setUnitPrice(item.getUnitPrice());
        itemDTO.setQuantity(item.getQuantity());
        //itemDTO.setImage(item.getImage());
        itemDTO.setMaterials(item.getMaterials().stream()
                .map(material -> material.getId())
                .toList());
        //itemDTO.setItemType(item.getItemType() == null ? null : item.getItemType().getId());
        return itemDTO;
    }

    private Item mapToEntity(final ItemCreateRecord itemDTO, final Item item, byte[] image) {
        item.setSku("AP-" + woodDensityRepository.findCodeById(itemDTO.density()) + "-" + itemDTO.size());
        item.setName(itemDTO.name().toUpperCase());
        item.setDensity(woodDensityRepository.findById(itemDTO.density()).orElseThrow(() -> new NotFoundException("Density not found")));
        item.setSize(itemDTO.size());
        item.setUnitPrice(itemDTO.unitPrice());
        item.setQuantity(itemDTO.quantity());
        item.setInUse(0);
        if(image != null) {
            item.setImage(image);
        }
        /**
        final List<Material> materials = materialRepository.findAllById(
                itemDTO.getMaterials() == null ? Collections.emptyList() : itemDTO.getMaterials());
        if (materials.size() != (itemDTO.getMaterials() == null ? 0 : itemDTO.getMaterials().size())) {
            throw new NotFoundException("one of materials not found");
        }
         **/
        item.setMaterials(new HashSet<>());
        /**
        final ItemType itemType = itemDTO.getItemType() == null ? null : itemTypeRepository.findById(itemDTO.getItemType())
                .orElseThrow(() -> new NotFoundException("itemType not found"));
        item.setItemType(itemType);
         **/
        return item;
    }

    public boolean skuExists(final String sku) {
        return itemRepository.existsBySkuIgnoreCase(sku);
    }

}
