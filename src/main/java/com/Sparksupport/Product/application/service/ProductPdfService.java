package com.sparksupport.product.application.service;

import com.sparksupport.product.application.model.Product;
import java.util.List;

public interface ProductPdfService {

    /**
     * Generate PDF synchronously with provided products
     */
    byte[] generateProductTablePdf(List<Product> products) throws Exception;

    /**
     * Submit async PDF generation job - backend will fetch data
     */
    String submitPdfGenerationJob();

    String submitPdfGenerationJob(List<Product> products);

    /**
     * Check status of PDF generation job
     */
    String checkJobStatus(String jobId);

    /**
     * Check if file exists and return file bytes if ready
     */
    byte[] getFileIfReady(String jobId) throws Exception;
}
