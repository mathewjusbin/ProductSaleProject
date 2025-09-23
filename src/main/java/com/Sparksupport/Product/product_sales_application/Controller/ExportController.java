package com.sparksupport.product.product_sales_application.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


import com.sparksupport.product.product_sales_application.service.ProductService;
import com.sparksupport.product.product_sales_application.serviceImpl.ProductPdfService;
import com.sparksupport.product.product_sales_application.model.Product;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;



@RestController
@RequestMapping("/api/reports")
public class ExportController {
    // Directory where we store exports
    private final Path exportDir = Paths.get("./exports");

    private final ProductService productService;
    private final ProductPdfService productPdfService;

    @Autowired
    public ExportController(ProductService productService) throws IOException {
        this.productService = productService;
        this.productPdfService = new ProductPdfService();
        // Ensure directory exists
        if (!Files.exists(exportDir)) {
            Files.createDirectories(exportDir);
        }
    }

    @GetMapping("/products/pdf/download")
    public ResponseEntity<StreamingResponseBody> downloadProductTablePdf() {
        try {
            // Fetch all products (no pagination)
            List<Product> products = productService.getAllProducts(org.springframework.data.domain.Pageable.unpaged()).getContent();
            byte[] pdfBytes = productPdfService.generateProductTablePdf(products);
            StreamingResponseBody stream = outputStream -> outputStream.write(pdfBytes);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=products-table.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(stream);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(outputStream -> outputStream.write(("Error: " + e.getMessage()).getBytes()));
        }
    }

    //end point to check status
}
