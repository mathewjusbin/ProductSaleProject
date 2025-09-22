package com.Sparksupport.Product.product_sales_application.Controller;

import com.Sparksupport.Product.product_sales_application.Dto.Product;
import com.Sparksupport.Product.product_sales_application.Dto.Sale;
import com.Sparksupport.Product.product_sales_application.Service.Patch;
import com.Sparksupport.Product.product_sales_application.Service.SaleService;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales")
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
    public ResponseEntity<?> addSale(
            @PathVariable @Min(value = 1, message = "productId must be >= 1") Integer id,
            @RequestBody @Valid Sale saleRequest) { //combination not working ---> review //Nothing is working
            Sale sale = saleService.addSales(id,saleRequest);
            return ResponseEntity.ok(sale);
    }

    /**
     * Update an existing sale (full replace).
     * PUT semantics: require all mandatory fields.
     */
    @PutMapping("/{saleId}")
    public ResponseEntity<?> updateSale(
            @PathVariable @Min(value = 1, message = "saleId must be >= 1") Integer saleId,
            @Valid @RequestBody Sale saleRequest) {
        System.out.println(" update sale ");
        try {
            Boolean result = saleService.updateSales(saleId, saleRequest);
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
