package com.sparksupport.product.product_sales_application.serviceImpl;

import com.sparksupport.product.product_sales_application.service.PdfExportTaskService;
import com.sparksupport.product.product_sales_application.config.PdfTaskManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class PdfExportTaskServiceImpl implements PdfExportTaskService {

    private final Path exportDir = Paths.get("./exports");
    private final PdfTaskManager taskManager;

    @Autowired
    public PdfExportTaskServiceImpl(PdfTaskManager taskManager) {
        this.taskManager = taskManager;

        try {
            if (!Files.exists(exportDir)) {
                Files.createDirectories(exportDir);
            }
        } catch (IOException e) {
            log.error("Failed to create export directory", e);
        }
    }

    @Override
    public void cleanupExpiredTasks() {
        log.info("Starting cleanup of expired PDF files and tasks");

        try {
            // Clean up files older than 24 hours by default
            cleanupFilesOlderThan(24);

            // Clean up completed job status entries older than 24 hours
            cleanupCompletedJobStatuses();

            log.info("Cleanup completed successfully. Active tasks: {}, Completed tasks: {}",
                    getActiveTaskCount(), getCompletedTaskCount());
        } catch (Exception e) {
            log.error("Error during cleanup process", e);
        }
    }

    @Override
    public void cleanupFilesOlderThan(int hours) {
        if (!Files.exists(exportDir)) {
            log.warn("Export directory does not exist: {}", exportDir);
            return;
        }

        Instant cutoffTime = Instant.now().minus(hours, ChronoUnit.HOURS);
        int deletedFiles = 0;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(exportDir, "*.pdf")) {
            for (Path file : stream) {
                try {
                    BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
                    Instant fileCreationTime = attrs.creationTime().toInstant();

                    if (fileCreationTime.isBefore(cutoffTime)) {
                        Files.delete(file);
                        deletedFiles++;
                        log.debug("Deleted expired PDF file: {}", file.getFileName());

                        // Extract jobId from filename and remove from status map
                        String fileName = file.getFileName().toString();
                        if (fileName.startsWith("products-report-") && fileName.endsWith(".pdf")) {
                            String jobId = fileName.substring("products-report-".length(),
                                                            fileName.length() - ".pdf".length());
                            taskManager.removeJob(jobId);
                        }
                    }
                } catch (IOException e) {
                    log.error("Error processing file: {}", file, e);
                }
            }
        } catch (IOException e) {
            log.error("Error reading export directory", e);
        }

        log.info("Deleted {} expired PDF files older than {} hours", deletedFiles, hours);
    }

    @Override
    public int getActiveTaskCount() {
        return (int) taskManager.getActiveTaskCount();
    }

    @Override
    public int getCompletedTaskCount() {
        return (int) taskManager.getCompletedTaskCount();
    }

    private void cleanupCompletedJobStatuses() {
        ConcurrentHashMap<String, String> jobStatusMap = taskManager.getJobStatusMap();

        // Remove COMPLETED and FAILED job statuses that don't have corresponding files
        jobStatusMap.entrySet().removeIf(entry -> {
            String jobId = entry.getKey();
            String status = entry.getValue();

            if ("COMPLETED".equals(status) || "FAILED".equals(status)) {
                String fileName = "products-report-" + jobId + ".pdf";
                Path filePath = exportDir.resolve(fileName);

                if (!Files.exists(filePath)) {
                    log.debug("Removing orphaned job status for jobId: {}", jobId);
                    return true;
                }
            }

            return false;
        });
    }
}
