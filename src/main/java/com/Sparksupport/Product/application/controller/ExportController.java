package com.sparksupport.product.application.controller;

import com.sparksupport.product.application.dto.ProductResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.HashMap;
import java.util.Map;

import com.sparksupport.product.application.service.ProductService;
import com.sparksupport.product.application.service.ProductPdfService;
import com.sparksupport.product.application.model.Product;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.sparksupport.product.application.util.ProductServiceUtil.SUCCESS;

@RestController
@RequestMapping("/api/reports")
public class ExportController {

    private final ProductService productService;
    private final ProductPdfService productPdfService;

    @Autowired
    public ExportController(ProductService productService, ProductPdfService productPdfService) {
        this.productService = productService;
        this.productPdfService = productPdfService;
    }

    //Sync end point for testing
    @GetMapping("/products/pdf/downloadSyncPdf")
    public ResponseEntity<StreamingResponseBody> downloadProductTablePdf() throws Exception {
        List<Product> products = productService.getAllProducts(org.springframework.data.domain.Pageable.unpaged()).getContent();

        // Force initialization of lazy collections
        for (Product product : products) {
            if (product.getSaleList() != null) {
                product.getSaleList().size();
            }
        }

        byte[] pdfBytes = productPdfService.generateProductTablePdf(products);
        StreamingResponseBody stream = outputStream -> outputStream.write(pdfBytes);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=products-table.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(stream);
    }

    // Endpoint 1: Submit PDF generation job
    @PostMapping("/products/pdf/generate")
    public ResponseEntity<?> submitPdfGenerationJob() {
        // Just submit the job, backend will handle data fetching
        String jobId = productPdfService.submitPdfGenerationJob();
        Map<String, String> response = new HashMap<>();
        response.put("jobId", jobId);
        response.put("status", "SUBMITTED");
        response.put("message", "PDF generation job submitted successfully");
        return ProductResponse.created(SUCCESS, response);
    }

    // Endpoint 2: Check job status
    @GetMapping("/products/pdf/status/{jobId}")
    public ResponseEntity<?> checkJobStatus(@PathVariable String jobId) {

        String status = productPdfService.checkJobStatus(jobId);

        Map<String, String> response = new HashMap<>();
        response.put("jobId", jobId);
        response.put("status", status);

        if ("COMPLETED".equals(status)) {
            response.put("message", "PDF generation completed successfully");
            response.put("downloadUrl", "/api/reports/products/pdf/file/" + jobId);
        } else if ("IN_PROGRESS".equals(status)) {
            response.put("message", "PDF generation is in progress");
        } else if ("FAILED".equals(status)) {
            response.put("message", "PDF generation failed");
        } else if ("NOT_FOUND".equals(status)) {
            response.put("message", "Job not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else {
            response.put("message", "Job status: " + status);
        }
        return ProductResponse.success(SUCCESS, response);

    }

    // Endpoint 3: Download file
    @GetMapping("/products/pdf/file/{jobId}")
    public ResponseEntity<StreamingResponseBody> downloadPdfFile(@PathVariable String jobId) throws Exception {

        // Check if file is ready and get the bytes
        byte[] pdfBytes = productPdfService.getFileIfReady(jobId);

        if (pdfBytes != null) {
            String fileName = "products-report-" + jobId + ".pdf";
            StreamingResponseBody stream = outputStream -> outputStream.write(pdfBytes);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(stream);
        } else {
            // File not ready - return simple error message like sync endpoint
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(outputStream -> outputStream.write("PDF file not ready or not found".getBytes()));
        }

    }
}
