package dev.twalidaziz.cms.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class OrderDTO {

    private Long id;

    @Size(max = 20)
    @OrderOrderNumberUnique
    private String orderNumber;

    @NotNull
    @Size(max = 50)
    private String description;

    @Digits(integer = 10, fraction = 2)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal totalCost;

    @NotNull
    @Size(max = 20)
    private String salesperson;

    @NotNull
    @Size(max = 50)
    private String contactPerson;

    @NotNull
    private OrderStatus status;

    //private List<Long> items; removed

    private Long customer;

    private OffsetDateTime lastUpdated;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(final String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(final BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public String getSalesperson() {
        return salesperson;
    }

    public void setSalesperson(final String salesperson) {
        this.salesperson = salesperson;
    }

    public String getContactPerson() { return contactPerson; }

    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(final OrderStatus status) {
        this.status = status;
    }
/**
    public List<Long> getItems() {
        return items;
    }

    public void setItems(final List<Long> items) {
        this.items = items;
    }
**/
    public Long getCustomer() {
        return customer;
    }

    public void setCustomer(final Long customer) {
        this.customer = customer;
    }

    public OffsetDateTime getLastUpdated() { return lastUpdated; }

    public void setLastUpdated(OffsetDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}
