package com.sparksupport.product.product_sales_application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Generic pagination request parameters")
public class PaginationRequest {

    @Min(value = 0, message = "Page number must be >= 0")
    @Schema(description = "Page number (0-based)", example = "0", defaultValue = "0")
    private int pageNumber = 0;

    @Min(value = 1, message = "Page size must be >= 1")
    @Max(value = 100, message = "Page size must be <= 100")
    @Schema(description = "Number of items per page", example = "10", defaultValue = "10")
    private int listSize = 10;
}
