package com.sparksupport.product.application.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Aspect
@Component
public class DebugLoggingAspect {

    // Create a specific logger for debug that will write to productlog.log
    private static final Logger debugLogger = LoggerFactory.getLogger("debug");

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM dd HH:mm:ss.SSSSSS");

    /**
     * Pointcut for all controller methods
     */
    @Pointcut("execution(* com.sparksupport.product.product_sales_application.controller.*.*(..))")
    public void controllerMethods() {}

    /**
     * Pointcut for all service methods
     */
    @Pointcut("execution(* com.sparksupport.product.product_sales_application.service.*.*(..)) || " +
              "execution(* com.sparksupport.product.product_sales_application.serviceImpl.*.*(..))")
    public void serviceMethods() {}

    /**
     * Pointcut for all repository/DAO methods
     */
    @Pointcut("execution(* com.sparksupport.product.product_sales_application.repository.*.*(..))")
    public void repositoryMethods() {}

    /**
     * Around advice for controller methods
     */
    @Around("controllerMethods()")
    public Object logControllerMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        return logMethodExecution(joinPoint, "CONTROLLER");
    }

    /**
     * Around advice for service methods
     */
    @Around("serviceMethods()")
    public Object logServiceMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        return logMethodExecution(joinPoint, "SERVICE");
    }

    /**
     * Around advice for repository/DAO methods
     */
    @Around("repositoryMethods()")
    public Object logRepositoryMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        return logMethodExecution(joinPoint, "DAO");
    }

    /**
     * Generic method to log method execution with entry, exit, and failure logging
     */
    private Object logMethodExecution(ProceedingJoinPoint joinPoint, String layerType) throws Throwable {
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);
        String threadName = Thread.currentThread().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        // Log method entry
        debugLogger.info("{} [{}], DEBUG, {}, {}, entry, DTO: {}",
            timestamp, threadName, className, methodName, formatArgs(args));

        try {
            // Execute the actual method
            Object result = joinPoint.proceed();

            // Log method exit with result
            debugLogger.info("{} [{}], INFO, {}, {}, exit, Result: {}",
                timestamp, threadName, className, methodName, formatResult(result));

            return result;

        } catch (Exception e) {
            // Log failure
            debugLogger.error("{} [{}], ERROR, {}, {}, failure, Exception: {} - {}",
                timestamp, threadName, className, methodName, e.getClass().getSimpleName(), e.getMessage());

            throw e; // Re-throw the exception
        }
    }

    /**
     * Format method arguments for logging
     */
    private String formatArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }

            Object arg = args[i];
            if (arg == null) {
                sb.append("null");
            } else if (isPrimitive(arg)) {
                sb.append(arg.toString());
            } else {
                // For complex objects, just show the class name and a brief representation
                sb.append(arg.getClass().getSimpleName()).append("@").append(Integer.toHexString(arg.hashCode()));
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Format method result for logging
     */
    private String formatResult(Object result) {
        if (result == null) {
            return "null";
        }

        if (isPrimitive(result)) {
            return result.toString();
        }

        // For complex objects, show class name and hash
        return result.getClass().getSimpleName() + "@" + Integer.toHexString(result.hashCode());
    }

    /**
     * Check if an object is a primitive type or String
     */
    private boolean isPrimitive(Object obj) {
        return obj instanceof String ||
               obj instanceof Number ||
               obj instanceof Boolean ||
               obj instanceof Character ||
               obj.getClass().isPrimitive();
    }
}
