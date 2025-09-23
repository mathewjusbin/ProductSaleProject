package com.sparksupport.product.product_sales_application.config;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Aspect
@Component
public class AuditAspect {

    // Create a specific logger for audit that will write to producaudit.log
    private static final Logger auditLogger = LoggerFactory.getLogger("audit");

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    /**
     * Pointcut for all controller methods
     */
    @Pointcut("execution(* com.sparksupport.product.product_sales_application.controller.*.*(..))")
    public void controllerMethods() {}

    /**
     * Around advice that wraps controller method execution
     */
    @Around("controllerMethods()")
    public Object auditControllerMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String txnId = UUID.randomUUID().toString().substring(0, 8); // Short transaction ID
        HttpServletRequest request = getCurrentRequest();
        String apiName = getApiName(joinPoint, request);
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);

        // Log REQUEST
        auditLogger.info("{}|{}|REQUEST|{}|", timestamp, apiName, txnId);

        try {
            // Execute the actual method
            Object result = joinPoint.proceed();

            // Log SUCCESS
            auditLogger.info("{}|{}|SUCCESS|{}|", timestamp, apiName, txnId);

            return result;

        } catch (Exception ex) {
            // Log FAILURE with error message
            auditLogger.info("{}|{}|FAILURE|{}|{}", timestamp, apiName, txnId, ex.getMessage());

            // Re-throw the exception
            throw ex;
        }
    }

    /**
     * Get API name from method and request
     */
    private String getApiName(ProceedingJoinPoint joinPoint, HttpServletRequest request) {
        String methodName = joinPoint.getSignature().getName();
        String httpMethod = request != null ? request.getMethod() : "UNKNOWN";
        String uri = request != null ? request.getRequestURI() : "N/A";

        // Format: GET_/api/products or POST_/api/products
        return httpMethod + "_" + uri.replaceAll("/", "_");
    }

    /**
     * Get current HTTP request
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return attributes.getRequest();
        } catch (Exception ex) {
            return null;
        }
    }
}
