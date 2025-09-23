package com.sparksupport.product.product_sales_application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto implements Serializable {

     @Serial
     private static final long serialVersionUID = 877388373L;

     @Schema(description = "Product ID", example = "1")
     private Integer id;

     @Schema(description = "Product name", example = "Premium Coffee Beans")
     private String name;

     @Schema(description = "Product description", example = "High quality premium coffee beans sourced from Brazil")
     private String description;

     @Schema(description = "Product price", example = "29.99")
     private Double price;

     @Schema(description = "Product quantity in stock", example = "100")
     private Integer quantity;
}