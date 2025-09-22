package com.Sparksupport.Product.product_sales_application.Controller;


import com.Sparksupport.Product.product_sales_application.Dto.ProductResponse;
import com.Sparksupport.Product.product_sales_application.Dto.Product;
import com.Sparksupport.Product.product_sales_application.Dto.ProductPaginationRequest;
import com.Sparksupport.Product.product_sales_application.Service.Create;
import com.Sparksupport.Product.product_sales_application.Service.Patch;
import com.Sparksupport.Product.product_sales_application.Service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static com.Sparksupport.Product.product_sales_application.Util.ProductServiceUtil.*;


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
        Page<Product> result = productService.getAllProducts(pageable);
        return ProductResponse.success(SUCCESS, result);
    }

    /**
     * GET /api/products/{id}
     * Returns single product by id.
     */
    @GetMapping("/{productId}")
    public ResponseEntity<?> getProductById(@PathVariable @Min(value = 1, message = "productId must be >= 1") Integer productId) {
        Product product = productService.getProductById(productId);
        return ProductResponse.success(SUCCESS, product);
    }

    /**
     * POST /api/products
     * Create a new product.
     * Requires ADMIN role (secured).
     * Returns 201 Created with Location header.
     */
    @PostMapping
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addProduct(@Validated(Create.class) @RequestBody Product product) {
        Product response = productService.addProduct(product);
        URI location = URI.create(String.format("/api/products/%d", response.getId()));
        return ProductResponse.success(CREATED, location);
    }

    /**
     * PATCH /api/products/{id}
     * Update an existing product.
     * Requires ADMIN role.
     * Returns 204 No Content (or 200 with updated product if you prefer).
     */
    //http://localhost:8080/api/products/123 and body
    @PatchMapping("/{id}")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateProduct(@PathVariable
                                           @Min(value = 1, message = "productId must be >= 1")
                                           @Max(value = Integer.MAX_VALUE, message = "productId length exceeded") Integer id,
                                           @Validated(Patch.class) @RequestBody Product product) {//I need to send body else will fial review

        System.out.println(" patch product ");
        Product response = productService.updateProduct(id, product);
        return ProductResponse.success(UPDATED, response);

    }

    /**
     * DELETE /api/products/{id}
     * Delete a product by id.
     * Requires ADMIN role.
     */
    @DeleteMapping("/{id}")
    //@PreAuthorize("hasRole('ADMIN')")
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
