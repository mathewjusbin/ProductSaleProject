package com.sparksupport.product.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sparksupport.product.application.service.Patch;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
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
@Schema(description = "DTO for updating sale information - only quantity and sale date can be modified")
public class UpdateSaleDto implements Serializable {

    private static final long serialVersionUID = 98366376L;

    @Min(value = 1, message = "Quantity must be greater than 0", groups = Patch.class)
    @Schema(description = "Updated quantity of products sold", example = "3")
    private Integer quantity;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Updated date and time of the sale",
            example = "2025-09-23T14:30:00",
            pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime saleDate;

    // Note: Sale price is not included as it's managed internally and shouldn't be modified by admin
}
