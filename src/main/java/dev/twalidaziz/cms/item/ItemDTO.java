package dev.twalidaziz.cms.item;

import com.fasterxml.jackson.annotation.JsonFormat;
import dev.twalidaziz.cms.order.OrderStatus;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;


public class ItemDTO {

    private Long id;

    @Size(max = 50)
    @ItemSkuUnique
    private String sku;

    @Size(max = 50)
    private String name;

    private Integer size;

    @Digits(integer = 10, fraction = 2)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal unitPrice;

    @NotNull
    private Integer quantity;

    @Size(max = 250)
    private String image;

    @NotNull
    private ItemCategory category;

    @NotNull
    private Long density;

    private List<Long> materials;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(final String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(final Integer size) {
        this.size = size;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(final BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(final Integer quantity) {
        this.quantity = quantity;
    }

    public String getImage() {
        return image;
    }

    public void setImage(final String image) {
        this.image = image;
    }

    public ItemCategory getCategory() { return category; }

    public void setCategory(ItemCategory category) { this.category = category; }

    public Long getDensity() { return density; }

    public void setDensity(Long density) { this.density = density; }

    public List<Long> getMaterials() {
        return materials;
    }

    public void setMaterials(final List<Long> materials) {
        this.materials = materials;
    }

}
