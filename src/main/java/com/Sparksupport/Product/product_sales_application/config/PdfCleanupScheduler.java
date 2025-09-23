package com.sparksupport.product.product_sales_application.config;

import com.sparksupport.product.product_sales_application.service.PdfExportTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class PdfCleanupScheduler {

    private final PdfExportTaskService taskService;

    @Scheduled(fixedRate = 3600000) // Run every hour
    public void cleanupExpiredPdfFiles() {
        taskService.cleanupExpiredTasks();
    }
}
