package com.sparksupport.product.product_sales_application.service;

public interface PdfExportTaskService {

    /**
     * Clean up expired PDF files and tasks
     */
    void cleanupExpiredTasks();

    /**
     * Clean up files older than specified hours
     */
    void cleanupFilesOlderThan(int hours);

    /**
     * Get count of active tasks
     */
    int getActiveTaskCount();

    /**
     * Get count of completed tasks
     */
    int getCompletedTaskCount();
}
