package com.sparksupport.product.product_sales_application.config;

import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PdfTaskManager {

    private final ConcurrentHashMap<String, String> jobStatusMap = new ConcurrentHashMap<>();

    public void setJobStatus(String jobId, String status) {
        jobStatusMap.put(jobId, status);
    }

    public String getJobStatus(String jobId) {
        return jobStatusMap.getOrDefault(jobId, "NOT_FOUND");
    }

    public void removeJob(String jobId) {
        jobStatusMap.remove(jobId);
    }

    public ConcurrentHashMap<String, String> getJobStatusMap() {
        return jobStatusMap;
    }

    public long getActiveTaskCount() {
        return jobStatusMap.values().stream()
                .filter(status -> "IN_PROGRESS".equals(status))
                .count();
    }

    public long getCompletedTaskCount() {
        return jobStatusMap.values().stream()
                .filter(status -> "COMPLETED".equals(status))
                .count();
    }
}
