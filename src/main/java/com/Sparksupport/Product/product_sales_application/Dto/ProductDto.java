package com.sparksupport.product.product_sales_application.dto;

import lombok.*;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@ToString
@Builder
public class ProductDto implements Serializable {

     private static final long serialVersionUID = 877388373L;

     private Integer id;

     private String name;

     private String description;

     private Double price;

     private Integer quantity;
}