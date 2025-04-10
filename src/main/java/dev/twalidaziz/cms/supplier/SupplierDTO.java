package dev.twalidaziz.cms.supplier;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public class SupplierDTO {

    private Long id;

    @NotNull
    @Size(max = 50)
    @SupplierNameUnique
    private String name;

    @NotNull
    @Size(max = 15)
    @SupplierSsmNumberUnique
    private String ssmNumber;

    @Size(max = 20)
    private String contactPerson;

    @Size(max = 50)
    private String email;

    @Size(max = 20)
    private String phone;

    @Size(max = 250)
    private String address;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getSsmNumber() {
        return ssmNumber;
    }

    public void setSsmNumber(final String ssmNumber) {
        this.ssmNumber = ssmNumber;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(final String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(final String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(final String address) {
        this.address = address;
    }

}
