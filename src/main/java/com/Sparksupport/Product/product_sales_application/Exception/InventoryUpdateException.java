package com.sparksupport.product.product_sales_application.exception;

public class InventoryUpdateException extends RuntimeException {
    private final Integer productId;
    private final Integer quantity;

    public InventoryUpdateException(Integer productId, Integer quantity) {
        super(String.format("Failed to update inventory for product ID %d with quantity %d", productId, quantity));
        this.productId = productId;
        this.quantity = quantity;
    }

    public Integer getProductId() {
        return productId;
    }

    public Integer getQuantity() {
        return quantity;
    }
}
