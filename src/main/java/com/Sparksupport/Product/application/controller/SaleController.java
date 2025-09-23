package com.sparksupport.product.application.controller;

import com.sparksupport.product.application.dto.*;
import com.sparksupport.product.application.model.Sale;
import com.sparksupport.product.application.service.Create;
import com.sparksupport.product.application.service.Patch;
import com.sparksupport.product.application.service.SaleService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.sparksupport.product.application.util.ProductServiceUtil.DELETED;
import static com.sparksupport.product.application.util.ProductServiceUtil.SUCCESS;

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
    @Operation(summary = "Add a new sale", description = "Create a new sale for a product")
    @SecurityRequirement(name = "Bearer Authentication")
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

        return ProductResponse.created(DELETED, responseDto);
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

        return ProductResponse.success(SUCCESS, responseDto);
    }

    /**
     * Delete a sale by id.
     */
    @DeleteMapping("/sales/{saleId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a sale", description = "Delete a sale by ID (Admin only)")
    @SecurityRequirement(name = "Bearer Authentication")
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
    public ResponseEntity<?> getSalesByProductId(
            @PathVariable @Min(value = 1, message = "productId must be >= 1") Integer productId,
            @Valid @ModelAttribute PaginationRequest paginationRequest) {

        Page<Sale> salesPage = saleService.getSalesByProductId(productId,
                PageRequest.of(paginationRequest.getPageNumber(), paginationRequest.getListSize()));

        return ProductResponse.success(SUCCESS, salesPage);
    }

    /**
     * GET /api/sales
     * Get all sales with pagination.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all sales", description = "Returns paginated list of all sales (Admin only)")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> getAllSales(@Valid @ModelAttribute PaginationRequest paginationRequest) {

        Page<Sale> salesPage = saleService.getAllSales(
                PageRequest.of(paginationRequest.getPageNumber(), paginationRequest.getListSize()));

        return ProductResponse.success(SUCCESS, salesPage);

    }
}

