package com.sparksupport.product.product_sales_application.controller;


import com.sparksupport.product.product_sales_application.dto.CreateProductDto;
import com.sparksupport.product.product_sales_application.dto.ProductResponse;
import com.sparksupport.product.product_sales_application.dto.ProductDto;
import com.sparksupport.product.product_sales_application.dto.PaginationRequest;
import com.sparksupport.product.product_sales_application.dto.UpdateProductDto;
import com.sparksupport.product.product_sales_application.model.Product;
import com.sparksupport.product.product_sales_application.service.Create;
import com.sparksupport.product.product_sales_application.service.Patch;
import com.sparksupport.product.product_sales_application.service.ProductService;
import com.sparksupport.product.product_sales_application.util.ProductServiceUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static com.sparksupport.product.product_sales_application.util.ProductServiceUtil.*;


@RestController
@RequestMapping("/api/products")
@Validated
@Tag(name = "Products", description = "Product management APIs")
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
    @Operation(summary = "Get all products", description = "Returns paginated list of products")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
    })
    public ResponseEntity<?> getAllProducts(@Valid @ModelAttribute PaginationRequest paginationRequest) {
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
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new product", description = "Create a new product (Admin only)")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Product created successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
            @ApiResponse(responseCode = "400", description = "Invalid product data")
    })
    public ResponseEntity<?> addProduct(@Validated(Create.class) @RequestBody CreateProductDto createProductDto) {
        Product response = productService.addProduct(createProductDto);
        URI location = URI.create(String.format("/api/products/%d", response.getId()));
        return ProductResponse.success(CREATED, location);
    }

    /**
     * PATCH /api/products/{id}
     * Update an existing product.
     * Requires ADMIN role.
     * Returns 200 OK with updated product details.
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Update an existing product",
        description = "Partially update a product by ID. Only provided fields will be updated. " +
                     "Requires ADMIN role authentication. ID is provided in URL path, not in request body.",
        tags = {"Products"}
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or validation errors"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "404", description = "Product not found with the given ID"),
            @ApiResponse(responseCode = "409", description = "Conflict - Product name already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> updateProduct(@PathVariable
                                           @Min(value = 1, message = "productId must be >= 1")
                                           @Max(value = Integer.MAX_VALUE, message = "productId length exceeded") Integer id,
                                           @Validated(Patch.class) @RequestBody UpdateProductDto updateProductDto) {
        Product product = Product.builder()
                .name(updateProductDto.getName())
                .description(updateProductDto.getDescription())
                .price(updateProductDto.getPrice())
                .quantity(updateProductDto.getQuantity())
                .isDeleted(Boolean.FALSE)
                .build();
        return ProductResponse.success(UPDATED,ProductServiceUtil.convertToProductDto(productService.updateProduct(id, product)) );
    }

    /**
     * DELETE /api/products/{id}
     * Delete a product by id.
     * Requires ADMIN role.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
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
        Double totalRevenue = productService.getTotalRevenue();
        return ProductResponse.success(SUCCESS, totalRevenue);
    }

    /**
     * GET /api/products/{id}/revenue
     * Returns revenue for a single product.
     */
    @GetMapping("/{productId}/revenue")
    public ResponseEntity<?> getRevenueByProduct(@PathVariable @Min(value = 1, message = "productId must be >= 1") Integer productId) {
        Double totalRevenue = productService.getRevenueByProduct(productId);
        return ProductResponse.success(SUCCESS, totalRevenue);
    }


}
