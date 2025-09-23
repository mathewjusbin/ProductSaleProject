package com.sparksupport.product.product_sales_application.exception;

public class SaleNotFoundException extends RuntimeException {
    private final Integer saleId;

    public SaleNotFoundException(Integer saleId) {
        super(String.format("Sale not found with id: %d", saleId));
        this.saleId = saleId;
    }

    public Integer getSaleId() {
        return saleId;
    }
}
