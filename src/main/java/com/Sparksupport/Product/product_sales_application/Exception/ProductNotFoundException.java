package com.sparksupport.product.product_sales_application.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Integer id) {
        super("Product not found for the id: " + id);
    }
}
