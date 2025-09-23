package com.sparksupport.product.application.dto;

import com.sparksupport.product.application.service.Patch;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for updating product information - ID is not included as it's provided in the URL path")
public class UpdateProductDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 877388375L;

    @Size(min = 2, max = 100, groups = Patch.class, message = "Name allowed size between 2 and 100")
    @Schema(description = "Product name", example = "Premium Coffee Beans")
    private String name;

    @Size(min = 2, max = 255, groups = Patch.class, message = "Description allowed size between 2 and 255")
    @Pattern(regexp = "^[a-zA-Z0-9 .,!?-]*$", groups = Patch.class,
            message = "Description having invalid characters")
    @Schema(description = "Product description", example = "High quality premium coffee beans sourced from Brazil")
    private String description;

    @DecimalMin(value = "0.0", groups = Patch.class, inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, groups = Patch.class, message = "Price can have only 10 digits and 2 fractions")
    @Schema(description = "Product price", example = "29.99")
    private Double price;

    @Min(value = 0, groups = Patch.class, message = "Quantity must be >= 0")
    @Max(value = 1000000, groups = Patch.class, message = "Quantity too large")
    @Schema(description = "Product quantity in stock", example = "100")
    private Integer quantity;
}
