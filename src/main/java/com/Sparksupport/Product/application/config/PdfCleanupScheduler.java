package com.sparksupport.product.application.config;

import com.sparksupport.product.application.service.PdfExportTaskService;
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
