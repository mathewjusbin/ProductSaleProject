package com.sparksupport.product.application.controller;


import com.sparksupport.product.application.dto.CreateProductDto;
import com.sparksupport.product.application.dto.ProductResponse;
import com.sparksupport.product.application.dto.PaginationRequest;
import com.sparksupport.product.application.dto.UpdateProductDto;
import com.sparksupport.product.application.model.Product;
import com.sparksupport.product.application.service.Create;
import com.sparksupport.product.application.service.Patch;
import com.sparksupport.product.application.service.ProductService;
import com.sparksupport.product.application.util.ProductServiceUtil;
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

import static com.sparksupport.product.application.util.ProductServiceUtil.*;


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

    /**
     * Health check endpoint to verify if the product controller is running properly.
     * This endpoint can be used for monitoring and health checks.
     *
     * @return ResponseEntity<String> with "controller OK" message
     */
    @GetMapping("/healthcheck")
    @Operation(summary = "Health check", description = "Check if the controller is running")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Controller is healthy")
    })
    public ResponseEntity<String> healthcheck() {
        return ResponseEntity.ok("controller OK");
    }

    /**
     * Retrieve all products with pagination support.
     * Returns a paginated list of all non-deleted products in the system.
     * Supports pagination parameters to control the number of results returned.
     *
     * @param paginationRequest Contains pageNumber (starting from 0) and listSize (1-100)
     * @return ResponseEntity<?> containing paginated list of ProductDto objects
     */
    @GetMapping()
    @Operation(summary = "Get all products", description = "Returns paginated list of products")
    public ResponseEntity<?> getAllProducts(@Valid @ModelAttribute PaginationRequest paginationRequest) {
        Pageable pageable = PageRequest.of(paginationRequest.getPageNumber(), paginationRequest.getListSize());
        return ProductResponse.success(SUCCESS, ProductServiceUtil.convertToProductDtoList(productService.getAllProducts(pageable)));
    }

    /**
     * Retrieve a specific product by its unique identifier.
     * Returns detailed information about a single product including its current inventory status.
     *
     * @param productId The unique identifier of the product (must be >= 1)
     * @return ResponseEntity<?> containing ProductDto if found
     * @throws IllegalArgumentException if product ID is invalid
     * @throws com.sparksupport.product.application.exception.ProductNotFoundException if product with given ID doesn't exist
     */
    @GetMapping("/{productId}")
    @Operation(
            summary = "Get product by ID",
            description = "Retrieve detailed information about a specific product by its unique identifier"
    )
    public ResponseEntity<?> getProductById(@PathVariable @Min(value = 1, message = "productId must be >= 1") Integer productId) { //TODO: Max
        return ProductResponse.success(SUCCESS, ProductServiceUtil.convertToProductDto(productService.getProductById(productId)));
    }

    /**
     * Create a new product in the system.
     * This endpoint allows administrators to add new products to the inventory.
     * All product details including name, description, price, and quantity must be provided.
     * Requires ADMIN role authentication via JWT token.
     *
     * @param createProductDto Contains product details: name, description, price, quantity
     * @return ResponseEntity<?> containing created product information with HTTP 201 status
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new product", description = "Create a new product (Admin only)")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> addProduct(@Validated(Create.class) @RequestBody CreateProductDto createProductDto) {
        Product response = productService.addProduct(createProductDto);
        URI location = URI.create(String.format("/api/products/%d", response.getId()));
        return ProductResponse.created(CREATED, location);
    }

    /**
     * Partially update an existing product by its ID.
     * Allows administrators to modify specific fields of a product without affecting other fields.
     * Only non-null fields in the request body will be updated.
     * The product ID is specified in the URL path, not in the request body.
     * Requires ADMIN role authentication via JWT token.
     *
     * @param id The unique identifier of the product to update (must be >= 1)
     * @param updateProductDto Contains optional fields to update: name, description, price, quantity
     * @return ResponseEntity<?> containing updated product information
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
     * Soft delete a product by marking it as deleted.
     * This operation doesn't permanently remove the product from the database,
     * but marks it as deleted so it won't appear in normal product listings.
     * Requires ADMIN role authentication via JWT token.
     *
     * @param id The unique identifier of the product to delete (must be >= 1)
     * @return ResponseEntity<?> with success message and deleted product ID
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete a product",
            description = "Soft delete a product by marking it as deleted. Requires ADMIN role authentication."
    )
    public ResponseEntity<?> deleteProduct(@PathVariable @Min(value = 1, message = "productId must be >= 1") Integer id) {
        productService.deleteProduct(id);
        return ProductResponse.success(DELETED, id);
    }

    /**
     * Calculate and retrieve the total revenue from all sales across all products.
     * Sums up the revenue generated from all non-deleted sales in the system.
     * Revenue is calculated as: sum of (sale_price * quantity) for all sales.
     *
     * @return ResponseEntity<?> containing total revenue as Double value
     */
    @GetMapping("/revenue/total")
    @Operation(summary = "Get total revenue", description = "Calculate total revenue from all sales")
    public ResponseEntity<?> getTotalRevenue() {
            System.out.println(" total revenue ");
            Double totalRevenue = productService.getTotalRevenue();
            return ProductResponse.success(SUCCESS, totalRevenue);
    }

    /**
     * Calculate and retrieve the total revenue generated by a specific product.
     * Sums up all revenue from sales of the specified product only.
     * Revenue is calculated as: sum of (sale_price * quantity) for all sales of this product.
     * Only includes non-deleted sales in the calculation.
     *
     * @param productId The unique identifier of the product (must be >= 1)
     * @return ResponseEntity<?> containing product-specific revenue as Double value
     */
    @GetMapping("/{productId}/revenue")
    @Operation(summary = "Get revenue by product", description = "Calculate revenue for a specific product")
    public ResponseEntity<?> getRevenueByProduct(@PathVariable @Min(value = 1, message = "productId must be >= 1") Integer productId) {
            Double totalRevenue = productService.getRevenueByProduct(productId);
            return ProductResponse.success(SUCCESS, totalRevenue);
    }

}
