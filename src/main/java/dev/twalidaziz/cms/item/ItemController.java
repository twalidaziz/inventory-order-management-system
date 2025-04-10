package dev.twalidaziz.cms.item;

import dev.twalidaziz.cms.order.*;
import dev.twalidaziz.cms.util.WebUtils;
import dev.twalidaziz.cms.wood_density.WoodDensity;
import dev.twalidaziz.cms.wood_density.WoodDensityListRecord;
import dev.twalidaziz.cms.wood_density.WoodDensityRepository;
import dev.twalidaziz.cms.wood_density.WoodDensityService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Controller
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;
    private final WoodDensityRepository woodDensityRepository;
    private final OrderService orderService;
    private final ItemRepository itemRepository;
    private final WoodDensityService woodDensityService;

    public ItemController(ItemService itemService, WoodDensityRepository woodDensityRepository, OrderService orderService, ItemRepository itemRepository, WoodDensityService woodDensityService) {
        this.itemService = itemService;
        this.woodDensityService = woodDensityService;
        this.woodDensityRepository = woodDensityRepository;
        this.orderService = orderService;
        this.itemRepository = itemRepository;
    }

    @GetMapping
    public String getItemCategoriesPage(final Model model) {

        model.addAttribute("totalItems", itemRepository.count());
        model.addAttribute("lowStockItems",itemRepository.countLowStock(5));
        model.addAttribute("noStockItems", itemRepository.countOutOfStock());

        return "inventory/item-categories";
    }

    @GetMapping("/list")
    public String list(final Model model,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(defaultValue = "lastUpdated") String sortBy,
                                 @RequestParam(defaultValue = "DESC") String direction) {

        Page<ItemListRecord> items = itemService.getAllItems(page, size, sortBy, direction);

        int totalItems = (int) items.getTotalElements();
        int startRecord = (page * size) + 1;
        int endRecord = Math.min(startRecord + size - 1, totalItems);

        model.addAttribute("items", items.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", items.getTotalPages());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("startRecord", startRecord);
        model.addAttribute("endRecord", endRecord);

        return "inventory/fragments/list :: mainContent";
    }

    @GetMapping("/list/{category}")
    public String listByCategory(final Model model,
                       @PathVariable ItemCategory category,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(defaultValue = "lastUpdated") String sortBy,
                       @RequestParam(defaultValue = "DESC") String direction) {

        Page<ItemListRecord> items = itemService.getItemsByCategory(category, page, size, sortBy, direction);

        model.addAttribute("items", items.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", items.getTotalPages());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        model.addAttribute("totalItems", items.getTotalElements());

        return "inventory/fragments/list :: itemList";
    }

    @GetMapping("/new")
    public String getNewItemForm(final Model model,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(defaultValue = "code") String sortBy,
                                 @RequestParam(defaultValue = "ASC") String direction) {

        Page<WoodDensityListRecord> woodPrices = woodDensityService.getAllWithNameIdCodeAndPrice(page, size, sortBy, direction);

        int totalWoodPrices = (int) woodPrices.getTotalElements();
        int startRecord = (page * size) + 1;
        int endRecord = Math.min(startRecord + size - 1, totalWoodPrices);

        model.addAttribute("item", new ItemCreateRecord(null, null, null, null, null, null));
        model.addAttribute("densities", woodDensityRepository.findAllWithNameIdAndCode());
        model.addAttribute("woodPrices", woodPrices);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", woodPrices.getTotalPages());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        model.addAttribute("totalWoodPrices", totalWoodPrices);
        model.addAttribute("startRecord", startRecord);
        model.addAttribute("endRecord", endRecord);

        return "inventory/fragments/new :: mainContent";
    }

    @PostMapping("/add")
    public String addItem(@ModelAttribute("item") @Valid final ItemCreateRecord itemDTO,
                          final BindingResult bindingResult,
                          final Model model,
                          @RequestParam(name="image", required = false) MultipartFile imageFile) {
        if(bindingResult.hasErrors()) {
            model.addAttribute("densities", woodDensityRepository.findAllWithNameIdAndCode());
            model.addAttribute("errorMessage", "Input required fields correctly.");
            return "inventory/fragments/new :: newForm";
        }

        try {
            byte[] imageBytes = (imageFile != null && !imageFile.isEmpty()) ? imageFile.getBytes() : null;

            Long newItemId = itemService.create(itemDTO, imageBytes);
            ItemEditRecord newItem = itemService.getItemById(newItemId);
            model.addAttribute("item", newItem);
            model.addAttribute("itemSKU", newItem.sku());
            model.addAttribute("itemImage", newItem.image());
            model.addAttribute("densities", woodDensityRepository.findAllWithNameIdAndCode());
            model.addAttribute("successMessage", "New item created.");
        } catch (Exception e) {
            model.addAttribute("imageFileError", "Error uploading image file");
        }
        return "inventory/fragments/edit :: editForm";
        //orderService.create(orderDTO);
        //return list(model, 0, 5, "lastUpdated", "DESC");
    }

    @GetMapping("/edit/{id}")
    public String getEditForm(@PathVariable(name = "id") Long id,
                              Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(defaultValue = "code") String sortBy,
                              @RequestParam(defaultValue = "ASC") String direction) {

        ItemEditRecord item = itemService.getItemById(id);
        Page<WoodDensityListRecord> woodPrices = woodDensityService.getAllWithNameIdCodeAndPrice(page, size, sortBy, direction);

        int totalWoodPrices = (int) woodPrices.getTotalElements();
        int startRecord = (page * size) + 1;
        int endRecord = Math.min(startRecord + size - 1, totalWoodPrices);

        model.addAttribute("item", item);
        model.addAttribute("itemSKU", item.sku());
        model.addAttribute("itemImage", item.image());
        model.addAttribute("densities", woodDensityRepository.findAllWithNameIdAndCode());

        model.addAttribute("woodPrices", woodPrices);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", woodPrices.getTotalPages());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        model.addAttribute("totalWoodPrices", totalWoodPrices);
        model.addAttribute("startRecord", startRecord);
        model.addAttribute("endRecord", endRecord);

        return "inventory/fragments/edit :: mainContent";
    }
    //TODO: change size datatype
    @PostMapping("/calculate-price")
    public String getPrice(@RequestParam("density") Long density,
                           @RequestParam("size") Integer size,
                           final Model model) {
        BigDecimal calculatedPrice = itemService.calculatePrice(density, size);

        model.addAttribute("calculatedPrice", calculatedPrice);
        return "inventory/fragments/new-price :: priceInput";
    }

    @PostMapping("/update/{id}")
    public String updateItem(@PathVariable("id") final Long id,
                         @Valid @ModelAttribute("item") final ItemUpdateRecord itemDTO,
                         final BindingResult bindingResult,
                         final Model model,
                         @RequestParam(name="image", required = false) MultipartFile imageFile,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "10") int size,
                         @RequestParam(defaultValue = "lastUpdated") String sortBy,
                         @RequestParam(defaultValue = "DESC") String direction) {
        // Validate user input
        if(bindingResult.hasErrors()) {
            ItemEditRecord currentItem = itemService.getItemById(id);
            model.addAttribute("itemSKU", currentItem.sku());
            model.addAttribute("itemImage", currentItem.image());
            model.addAttribute("densities", woodDensityRepository.findAllWithNameIdAndCode());
            model.addAttribute("errorMessage", "Please input required fields correctly.");

            return "inventory/fragments/edit :: editForm";
        }

        try {
            byte[] imageBytes = (imageFile != null && !imageFile.isEmpty()) ? imageFile.getBytes() : null;

            itemService.update(id, itemDTO, imageBytes);
            ItemEditRecord updatedItem = itemService.getItemById(id);
            model.addAttribute("itemSKU", updatedItem.sku());
            model.addAttribute("itemImage", updatedItem.image());
            model.addAttribute("densities", woodDensityRepository.findAllWithNameIdAndCode());
            model.addAttribute("successMessage", "Item details saved.");
        } catch (Exception e) {
            model.addAttribute("imageFileError", "Error uploading image file");
        }
        return "inventory/fragments/edit :: editForm";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable(name = "id") final Long id,
                         final RedirectAttributes redirectAttributes) {
        orderService.delete(id);
        redirectAttributes.addFlashAttribute(WebUtils.MSG_INFO, WebUtils.getMessage("order.delete.success"));
        return "redirect:/orders";
    }

    @GetMapping("/search")
    public String searchItems(final Model model,
                               @RequestParam(name = "keyword") String keyword,
                               @RequestParam(defaultValue ="0") int page,
                               @RequestParam(defaultValue = "5") int size,
                               @RequestParam(defaultValue = "lastUpdated") String sortBy,
                               @RequestParam(defaultValue = "DESC") String direction) {

        Page<ItemListRecord> items = itemService.getItemBySkuOrName(keyword, page, size, sortBy, direction);

        long totalElements = items.getTotalElements();

        if(totalElements == 0) {
            model.addAttribute("message", "Item not found");
            return "inventory/fragments/item-not-found :: mainContent";
        } else {
            int totalItems = (int) totalElements;
            int startRecord = (page * size) + 1;
            int endRecord = Math.min(startRecord + size - 1, totalItems);

            model.addAttribute("items", items.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", items.getTotalPages());
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("direction", direction);
            model.addAttribute("totalItems", totalItems);
            model.addAttribute("startRecord", startRecord);
            model.addAttribute("endRecord", endRecord);
        }
        return "inventory/fragments/list :: itemList";
    }

    @GetMapping("/search-wood-pricing")
    public String searchWoodPricing(final Model model,
                               @RequestParam(name = "keyword") String keyword,
                               @RequestParam(defaultValue ="0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(defaultValue = "code") String sortBy,
                               @RequestParam(defaultValue = "ASC") String direction) {

        Page<WoodDensityListRecord> woodPrices = woodDensityService.getAllByCodeOrName(
                keyword, page, size, sortBy, direction
        );

        long totalElements = woodPrices.getTotalElements();

        if(totalElements == 0) {
            model.addAttribute("message", "Wood pricing not found.");
            return "inventory/fragments/search-error :: mainContent";
        } else {
            int totalWoodPrices = (int) totalElements;
            int startRecord = (page * size) + 1;
            int endRecord = Math.min(startRecord + size - 1, totalWoodPrices);

            model.addAttribute("woodPrices", woodPrices);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", woodPrices.getTotalPages());
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("direction", direction);
            model.addAttribute("totalWoodPrices", totalWoodPrices);
            model.addAttribute("startRecord", startRecord);
            model.addAttribute("endRecord", endRecord);
        }
        return "inventory/fragments/edit :: woodPriceList";
    }

    @GetMapping("/refresh")
    public String refresh(final Model model,
                          @RequestParam(defaultValue ="0") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(defaultValue = "code") String sortBy,
                          @RequestParam(defaultValue = "ASC") String direction) {

        Page<WoodDensityListRecord> woodPrices = woodDensityService.getAllWithNameIdCodeAndPrice(page, size, sortBy, direction);

        int totalWoodPrices = (int) woodPrices.getTotalElements();
        int startRecord = (page * size) + 1;
        int endRecord = Math.min(startRecord + size - 1, totalWoodPrices);

        model.addAttribute("woodPrices", woodPrices);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", woodPrices.getTotalPages());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        model.addAttribute("totalWoodPrices", totalWoodPrices);
        model.addAttribute("startRecord", startRecord);
        model.addAttribute("endRecord", endRecord);

        return "inventory/fragments/edit :: woodPricing";
    }
}
