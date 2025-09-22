package com.sparksupport.product.product_sales_application.controller;


import com.Sparksupport.Product.product_sales_application.Dto.ProductResponse;
import com.sparksupport.product.product_sales_application.dto.ProductDto;
import com.sparksupport.product.product_sales_application.dto.ProductPaginationRequest;
import com.sparksupport.product.product_sales_application.model.Product;
import com.sparksupport.product.product_sales_application.service.Create;
import com.sparksupport.product.product_sales_application.service.Patch;
import com.sparksupport.product.product_sales_application.service.ProductService;
import com.sparksupport.product.product_sales_application.util.ProductServiceUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static com.sparksupport.product.product_sales_application.util.ProductServiceUtil.*;


@RestController
@RequestMapping("/api/products")
@Validated
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/healthcheck")
    public String healthcheck() {
        return "controller OK";
    }

    /**
     * GET /api/products
     * Returns paginated list of products.
     */
    @GetMapping()
    public ResponseEntity<?> getAllProducts(@Valid @ModelAttribute ProductPaginationRequest paginationRequest) {
        Pageable pageable = PageRequest.of(paginationRequest.getPageNumber(), paginationRequest.getListSize()); 
        return ProductResponse.success(SUCCESS, ProductServiceUtil.convertToProductDtoList(productService.getAllProducts(pageable)));
    }

    /**
     * GET /api/products/{id}
     * Returns single product by id.
     */
    @GetMapping("/{productId}")
    public ResponseEntity<?> getProductById(@PathVariable @Min(value = 1, message = "productId must be >= 1") Integer productId) { //TODO: Max
        return ProductResponse.success(SUCCESS, ProductServiceUtil.convertToProductDto(productService.getProductById(productId)));
    }

    /**
     * POST /api/products
     * Create a new product.
     * Requires ADMIN role (secured).
     * Returns 201 Created with Location header.
     */
    @PostMapping
    public ResponseEntity<?> addProduct(@Validated(Create.class) @RequestBody ProductDto productDto) {
        Product response = productService.addProduct(productDto);
        URI location = URI.create(String.format("/api/products/%d", response.getId()));
        return ProductResponse.success(CREATED, location);
    }

    /**
     * PATCH /api/products/{id}
     * Update an existing product.
     * Requires ADMIN role.
     * Returns 204 No Content (or 200 with updated product if you prefer).
     */
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable
                                           @Min(value = 1, message = "productId must be >= 1")
                                           @Max(value = Integer.MAX_VALUE, message = "productId length exceeded") Integer id,
                                           @Validated(Patch.class) @RequestBody ProductDto productDto) {//I need to send body else will fial review
        Product product = Product.builder()
                .Id(productDto.getId())
                .name(productDto.getName())
                .description(productDto.getDescription())
                .isDeleted(Boolean.FALSE)
                .price(productDto.getPrice())
                .build();
        return ProductResponse.success(UPDATED,ProductServiceUtil.convertToProductDto(productService.updateProduct(id, product)) );
    }

    /**
     * DELETE /api/products/{id}
     * Delete a product by id.
     * Requires ADMIN role.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable @Min(value = 1, message = "productId must be >= 1") Integer id) {
        productService.deleteProduct(id);
        return ProductResponse.success(DELETED, id);
    }

    /**
     * GET /api/products/revenue/total
     * Returns total revenue across all products.
     */
    @GetMapping("/revenue/total")
    public ResponseEntity<?> getTotalRevenue() {
        System.out.println(" total revenue ");
        Double total = productService.getTotalRevenue();
        return ResponseEntity.ok(total);
    }

    /**
     * GET /api/products/{id}/revenue
     * Returns revenue for a single product.
     */
    @GetMapping("/{productId}/revenue")
    public ResponseEntity<?> getRevenueByProduct(@PathVariable @Min(value = 1, message = "productId must be >= 1") Integer productId) {
        Double revenue = productService.getRevenueByProduct(productId);
        return ResponseEntity.ok(revenue);
    }


}
