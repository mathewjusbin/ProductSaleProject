package com.sparksupport.product.product_sales_application.controller;

import com.sparksupport.product.product_sales_application.dto.SaleDto;
import com.sparksupport.product.product_sales_application.model.Sale;
import com.sparksupport.product.product_sales_application.service.SaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping("/{id}")
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
            @PathVariable @Min(value = 1, message = "productId must be >= 1") Integer id,
            @RequestBody @Valid SaleDto saleRequest) { //combination not working ---> review //Nothing is working
            
            // Convert SaleDto to Sale model
            Sale sale = new Sale();
            sale.setProductId(saleRequest.getProductId());
            sale.setQuantity(saleRequest.getQuantity());
            sale.setSaleDate(saleRequest.getSaleDate());
            sale.setSalePrice(saleRequest.getSalePrice());
            
            Sale savedSale = saleService.addSales(id, sale);
            
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
     * Update an existing sale (full replace).
     * PUT semantics: require all mandatory fields.
     */
    @PutMapping("/{saleId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a sale", description = "Update an existing sale (Admin only)")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sale updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "404", description = "Sale not found")
    })
    public ResponseEntity<?> updateSale(
            @PathVariable @Min(value = 1, message = "saleId must be >= 1") Integer saleId,
            @Valid @RequestBody SaleDto saleRequest) {
        System.out.println(" update sale ");
        try {
            // Convert SaleDto to Sale model
            Sale sale = new Sale();
            sale.setId(saleId);
            sale.setProductId(saleRequest.getProductId());
            sale.setQuantity(saleRequest.getQuantity());
            sale.setSaleDate(saleRequest.getSaleDate());
            sale.setSalePrice(saleRequest.getSalePrice());
            
            Boolean result = saleService.updateSales(saleId, sale);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred: " + e.getMessage());
        }
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
        System.out.println(" update sale ");
        try {
            Boolean result =  saleService.deleteSales(saleId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred: " + e.getMessage());
        }
    }

}
