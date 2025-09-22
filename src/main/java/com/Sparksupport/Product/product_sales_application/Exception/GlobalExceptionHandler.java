package com.sparksupport.product.product_sales_application.exception;

import com.Sparksupport.Product.product_sales_application.Dto.ProductResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

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
