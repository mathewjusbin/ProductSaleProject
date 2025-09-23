package com.sparksupport.product.product_sales_application.exception;

public class InsufficientStockException extends RuntimeException {
    private final Integer availableQuantity;
    private final Integer requestedQuantity;
    private final Integer productId;

    public InsufficientStockException(Integer productId, Integer availableQuantity, Integer requestedQuantity) {
        super(String.format("Insufficient stock for product ID %d. Available: %d, Requested: %d",
              productId, availableQuantity, requestedQuantity));
        this.productId = productId;
        this.availableQuantity = availableQuantity;
        this.requestedQuantity = requestedQuantity;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public Integer getRequestedQuantity() {
        return requestedQuantity;
    }

    public Integer getProductId() {
        return productId;
    }
}
