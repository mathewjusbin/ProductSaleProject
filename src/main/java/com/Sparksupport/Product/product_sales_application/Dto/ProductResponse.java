package com.sparksupport.product.product_sales_application.dto;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ProductResponse<T> {

    private int status;
    private String message;
    private T data;  // optional payload

    public ProductResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public T getData() { return data; }

    public static <T> ResponseEntity<ProductResponse<T>> success(String message, T data) {
        return ResponseEntity.ok(new ProductResponse<>(HttpStatus.OK.value(), message, data));
    }

    public static <T> ResponseEntity<ProductResponse<T>> created(String message, T data) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ProductResponse<>(HttpStatus.CREATED.value(), message, data));
    }

    public static ResponseEntity<ProductResponse<Object>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new ProductResponse<>(status.value(), message, ""));
    }
}
