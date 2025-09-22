package com.sparksupport.product.product_sales_application.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.sparksupport.product.product_sales_application.service.Create;
import com.sparksupport.product.product_sales_application.service.Patch;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "Product", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name")
}) //Not working
@Builder
@AllArgsConstructor
public class Product implements Serializable {

    @Serial
    private static final long serialVersionUID = 877388373L;

    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer Id;

    @NotBlank(message = "Name is mandatory", groups = Create.class)
    @Size(min = 2, max = 100, groups = {Create.class, Patch.class}, message = "Name allowed size between 2 and 100 ")
    private String name;

    @NotBlank(message = "Description is mandatory", groups = Create.class)
    @Size(min = 2, max = 255, groups = {Create.class, Patch.class}, message = "Description allowed size between 2 and 255")
    @Pattern(regexp = "^[a-zA-Z0-9 .,!?-]*$", groups = {Create.class, Patch.class},
            message = "Description having invalid characters")  //Regx handling
    private String description;

    @NotNull(message = "Price is required", groups = Create.class) //here integer any issue need to see Review
    @DecimalMin(value = "0.0", groups = {Create.class, Patch.class}, inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, groups = {Create.class, Patch.class}, message = "Price can have only 10 digits and 2 fractions")
    private Double price;

    @NotNull(message = "Quantity is required", groups = Create.class)
    @Min(value = 0, groups = {Create.class, Patch.class}, message = "Quantity must be >= 0")
    @Max(value = 1000000, groups = {Create.class, Patch.class}, message = "Quantity too large")
    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 0; // inventory count

    // Fixed the @OneToMany mapping - removed the incorrect mappedBy since Sale doesn't have a Product property
    // Using @JoinColumn instead to map by product_id foreign key
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id") // This maps to the productId field in Sale entity
    @JsonManagedReference // prevents recursion when returning Product entity as JSON
    private List<Sale> saleList;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    public Product(){ // why mahn

    }
    public Product(Integer id, String name, String description, Double price) {
        Id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = 0; // default quantity
    } //Not adding for sales since for creation sale is not required.

    public Product(Integer id, String name, String description, Double price, Integer quantity) {
        Id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
    }

    public Integer getId() {
        return Id;
    }

    public void setId(Integer id) {
        Id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    // Method to reduce quantity during sale
    public boolean reduceQuantity(Integer saleQuantity) {
        if (this.quantity >= saleQuantity) {
            this.quantity -= saleQuantity;
            return true;
        }
        return false; // insufficient stock
    }

    public List<Sale> getSaleList() {
        return saleList;
    }

    public void setSaleList(List<Sale> saleList) {
        this.saleList = saleList;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    @Override
    public String toString() {
        return "Product{" +
                "Id=" + Id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                '}';
    }
}
