package com.sparksupport.product.product_sales_application.dto;

import com.sparksupport.product.product_sales_application.service.Create;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "DTO for creating a new sale - Product ID and sale price are determined from the product in database")
public class CreateSaleDto implements Serializable {

    private static final long serialVersionUID = 98366375L;

    @NotNull(message = "Quantity is required", groups = Create.class)
    @Min(value = 1, message = "Quantity must be greater than 0", groups = Create.class)
    @Schema(description = "Quantity of products sold", example = "5", required = true)
    private Integer quantity;

    @NotNull(message = "Sale date is required", groups = Create.class)
    @Schema(description = "Date and time of the sale", example = "2025-09-23T10:30:00", required = true)
    private LocalDateTime saleDate;

    // Removed salePrice field - it will be fetched from the product in database
}
