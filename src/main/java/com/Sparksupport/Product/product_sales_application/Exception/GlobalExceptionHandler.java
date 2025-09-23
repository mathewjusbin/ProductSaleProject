package com.sparksupport.product.product_sales_application.exception;

import com.sparksupport.product.product_sales_application.dto.ProductResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errorMap = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fieldError ->
                errorMap.put(fieldError.getField(), fieldError.getDefaultMessage())
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMap);
    }

    // Handle method parameter constraints (e.g. @Min on @PathVariable / @RequestParam)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errorMap = new HashMap<>();
        for (ConstraintViolation<?> cv : ex.getConstraintViolations()) {
            String rawPath = cv.getPropertyPath().toString();
            String field = rawPath.contains(".") ? rawPath.substring(rawPath.lastIndexOf('.') + 1) : rawPath;
            errorMap.put(field, cv.getMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMap);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex) {
        System.out.println(" her  her");
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("status", String.valueOf(HttpStatus.CONFLICT.value()));  // 409 Conflict
        errorMap.put("error", "Conflict");
        errorMap.put("message", ex.getMessage());
        return new ResponseEntity<>(errorMap, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ProductResponse<Object>> handleProductNotFound(ProductNotFoundException ex) {
        return ProductResponse.error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(SaleNotFoundException.class)
    public ResponseEntity<Object> handleSaleNotFound(SaleNotFoundException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", HttpStatus.NOT_FOUND.value());
        errorResponse.put("error", "Sale Not Found");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("saleId", ex.getSaleId());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Object> handleInsufficientStock(InsufficientStockException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Insufficient Stock");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("productId", ex.getProductId());
        errorResponse.put("availableQuantity", ex.getAvailableQuantity());
        errorResponse.put("requestedQuantity", ex.getRequestedQuantity());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InventoryUpdateException.class)
    public ResponseEntity<Object> handleInventoryUpdate(InventoryUpdateException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", HttpStatus.CONFLICT.value());
        errorResponse.put("error", "Inventory Update Failed");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("productId", ex.getProductId());
        errorResponse.put("quantity", ex.getQuantity());
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    // Handle JSON parsing errors (e.g., invalid date format)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleJsonParseError(HttpMessageNotReadableException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Invalid Request Format");

        String message = ex.getMessage();
        if (message != null && message.contains("LocalDateTime")) {
            errorResponse.put("message", "Invalid date format. Please use ISO format: yyyy-MM-ddTHH:mm:ss (e.g., 2025-09-23T08:13:15)");
            errorResponse.put("field", "saleDate");
        } else if (message != null && message.contains("JSON parse error")) {
            errorResponse.put("message", "Invalid JSON format in request body");
        } else {
            errorResponse.put("message", "Invalid request format");
        }

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Handle generic exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneralException(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Handle generic exceptions
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<Object> handleSystemExcption(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }


}
