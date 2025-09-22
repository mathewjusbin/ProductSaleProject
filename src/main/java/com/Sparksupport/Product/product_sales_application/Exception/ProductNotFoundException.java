package com.Sparksupport.Product.product_sales_application.Exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Integer id) {
        super("Product not found for the id: " + id);
    }
}
