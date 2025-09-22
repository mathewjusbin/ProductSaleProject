package com.Sparksupport.Product.product_sales_application.Dto;

import com.Sparksupport.Product.product_sales_application.Service.Create;
import com.Sparksupport.Product.product_sales_application.Service.Patch;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "Product", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name")
}) //Not working
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

    //@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OneToMany(mappedBy = "product")
    @JsonManagedReference // prevents recursion when returning Product entity as JSON
    private List<Sale> saleList;

    public Product(){ // why mahn

    }
    public Product(Integer id, String name, String description, Double price) {
        Id = id;
        this.name = name;
        this.description = description;
        this.price = price;
    } //Not adding for sales since for creation sale is not required.

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

    public List<Sale> getSaleList() {
        return saleList;
    }

    public void setSaleList(List<Sale> saleList) {
        this.saleList = saleList;
    }

    @Override
    public String toString() {
        return "Product{" +
                "Id=" + Id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                '}';
    }
}
