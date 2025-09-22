package com.Sparksupport.Product.product_sales_application.Util;

import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.Map;

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


}
