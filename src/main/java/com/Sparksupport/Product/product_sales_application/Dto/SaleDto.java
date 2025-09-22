package com.sparksupport.product.product_sales_application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
public class SaleDto implements Serializable {

    private static final long serialVersionUID = 98366374L;

    private Integer Id;

    private Integer productId;

    private Integer Quantity;

    private LocalDateTime saleDate;

    private BigDecimal salePrice;

    public SaleDto(){ //why that error came mahn

    }



}
