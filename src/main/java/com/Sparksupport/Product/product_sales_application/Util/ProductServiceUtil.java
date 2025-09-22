package com.sparksupport.product.product_sales_application.util;

import com.sparksupport.product.product_sales_application.dto.ProductDto;
import com.sparksupport.product.product_sales_application.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProductServiceUtil {

    public static final String OPERATION = "OPERATION";
    public static final String SUCCESS = "SUCCESS";
    public static final String FAILURE = "FAILURE";
    public static final String STATUS = "STATUS";
    public static final String MESSAGE = "MESSAGE";
    public static final String DELETED = "DELETED";
    public static final String CREATED = "CREATED";
    public static final String UPDATED = "UPDATED";

    public static ResponseEntity<?>  respond(String operation, String message) {
        Map<String, String> response = new HashMap<>();
        response.put(STATUS, String.valueOf(HttpStatus.OK.value()));
        response.put(MESSAGE, "Product deleted successfully");
        return ResponseEntity.ok(response); //make it global later
    }

    public static ProductDto convertToProductDto(Product product) {
        return ProductDto.builder()
                .description(Optional.ofNullable(product.getDescription()).orElse("No Description"))
                .id(Optional.ofNullable(product.getId()).orElse(0))
                .name(Optional.ofNullable(product.getName()).orElse("No Name"))
                .price(Optional.ofNullable(product.getPrice()).orElse(0.0))
                .build();
    }

    public static List<ProductDto> convertToProductDtoList( Page<Product> products) {
        return products.stream()
                .map(ProductServiceUtil::convertToProductDto)
                .collect(Collectors.toList());
    }
}
