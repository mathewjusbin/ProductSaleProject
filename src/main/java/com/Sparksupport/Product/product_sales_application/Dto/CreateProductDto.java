package com.sparksupport.product.product_sales_application.dto;

import com.sparksupport.product.product_sales_application.service.Create;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 877388374L;

    @NotBlank(message = "Name is mandatory", groups = Create.class)
    @Size(min = 2, max = 100, groups = Create.class, message = "Name allowed size between 2 and 100")
    @Schema(description = "Product name", example = "Premium Coffee Beans")
    private String name;

    @NotBlank(message = "Description is mandatory", groups = Create.class)
    @Size(min = 2, max = 255, groups = Create.class, message = "Description can only contain letters, numbers, spaces, and basic punctuation")
    @Pattern(regexp = "^[a-zA-Z0-9 .,!?-]*$", groups = Create.class,
            message = "Description having invalid characters")
    @Schema(description = "Product description", example = "High quality premium coffee beans sourced from Brazil")
    private String description;

    @NotNull(message = "Price is required", groups = Create.class)
    @DecimalMin(value = "0.0", groups = Create.class, inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, groups = Create.class, message = "Price can have only 10 digits and 2 fractions")
    @Schema(description = "Product price", example = "29.99")
    private Double price;

    @NotNull(message = "Quantity is required", groups = Create.class)
    @Min(value = 0, groups = Create.class, message = "Quantity must be >= 0")
    @Max(value = 1000000, groups = Create.class, message = "Quantity too large")
    @Schema(description = "Product quantity in stock", example = "100")
    private Integer quantity;
}
