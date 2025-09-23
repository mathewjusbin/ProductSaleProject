package com.sparksupport.product.product_sales_application.controller;

import com.sparksupport.product.product_sales_application.dto.*;
import com.sparksupport.product.product_sales_application.model.Sale;
import com.sparksupport.product.product_sales_application.service.Create;
import com.sparksupport.product.product_sales_application.service.Patch;
import com.sparksupport.product.product_sales_application.service.SaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.sparksupport.product.product_sales_application.util.ProductServiceUtil.DELETED;

@RestController
@RequestMapping("/api/sales")
@Tag(name = "Sales", description = "Sales management operations")
public class SaleController {

    private final SaleService saleService;

    @Autowired
    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    /**
     * Add a sale for a product.
     * productId from path, sale details in body.
     */
    @PostMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a new sale", description = "Create a new sale for a product (Admin only)")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sale created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<?> addSale(
            @PathVariable @Min(value = 1, message = "productId must be >= 1") Integer productId,
            @RequestBody @Validated(Create.class) CreateSaleDto saleRequest) {

        // Convert CreateSaleDto to Sale model
        Sale sale = new Sale();
        sale.setProductId(productId); // Use productId from path variable, not from request body
        sale.setQuantity(saleRequest.getQuantity());
        sale.setSaleDate(saleRequest.getSaleDate());
        // Removed setSalePrice - will be set automatically from product price in service layer

        Sale savedSale = saleService.addSales(productId, sale);

        // Convert back to SaleDto for response
        SaleDto responseDto = new SaleDto();
        responseDto.setId(savedSale.getId());
        responseDto.setProductId(savedSale.getProductId());
        responseDto.setQuantity(savedSale.getQuantity());
        responseDto.setSaleDate(savedSale.getSaleDate());
        responseDto.setSalePrice(savedSale.getSalePrice());

        return ResponseEntity.ok(responseDto);
    }

    /**
     * PATCH /api/sales/{saleId}
     * Partially update an existing sale (quantity and/or date only).
     * Allows admin to correct mistakes in quantity or sale date.
     */
    @PatchMapping("/{saleId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update sale quantity or date",
            description = "Partially update a sale - only quantity and sale date can be modified. " +
                    "Both fields are optional for partial updates. Sale price remains unchanged as it's managed internally. " +
                    "Use this to correct mistakes in quantity or sale date (Admin only)."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sale updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data or validation errors"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "404", description = "Sale not found")
    })
    public ResponseEntity<?> updateSale(
            @PathVariable @Min(value = 1, message = "saleId must be >= 1") Integer saleId,
            @Validated(Patch.class) @RequestBody UpdateSaleDto updateSaleRequest) {


        Sale updatedSale = saleService.updateSales(saleId, updateSaleRequest);

        // Convert to response DTO (without exposing sale price to client)
        SaleDto responseDto = new SaleDto();
        responseDto.setId(updatedSale.getId());
        responseDto.setProductId(updatedSale.getProductId());
        responseDto.setQuantity(updatedSale.getQuantity());
        responseDto.setSaleDate(updatedSale.getSaleDate());
        // Intentionally not setting salePrice - it's internal and shouldn't be exposed to client

        return ResponseEntity.ok(responseDto);
    }

    /**
     * Delete a sale by id.
     */
    @DeleteMapping("/sales/{saleId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a sale", description = "Delete a sale by ID (Admin only)")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sale deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "404", description = "Sale not found")
    })
    public ResponseEntity<?> deleteSale(
            @PathVariable @Min(value = 1, message = "saleId must be >= 1") Integer saleId) {

        saleService.deleteSales(saleId);
        return ProductResponse.success(DELETED, saleId);

    }

    /**
     * GET /api/sales/product/{productId}
     * Get paginated sales for a specific product.
     */
    @GetMapping("/product/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get sales by product ID", description = "Returns paginated list of sales for a specific product (Admin only)")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sales retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<?> getSalesByProductId(
            @PathVariable @Min(value = 1, message = "productId must be >= 1") Integer productId,
            @Valid @ModelAttribute PaginationRequest paginationRequest) {

        Page<Sale> salesPage = saleService.getSalesByProductId(productId,
                PageRequest.of(paginationRequest.getPageNumber(), paginationRequest.getListSize()));

        return ResponseEntity.ok(salesPage);
    }

    /**
     * GET /api/sales
     * Get all sales with pagination.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all sales", description = "Returns paginated list of all sales (Admin only)")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sales retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<?> getAllSales(@Valid @ModelAttribute PaginationRequest paginationRequest) {

        Page<Sale> salesPage = saleService.getAllSales(
                PageRequest.of(paginationRequest.getPageNumber(), paginationRequest.getListSize()));

        return ResponseEntity.ok(salesPage);
    }
}
