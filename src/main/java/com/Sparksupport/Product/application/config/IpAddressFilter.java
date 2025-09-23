package com.sparksupport.product.application.config;

import com.sparksupport.product.application.util.IpAddressUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class IpAddressFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(IpAddressFilter.class);

    @Autowired
    private SecurityProperties securityProperties;

    @Autowired
    private IpAddressUtil ipAddressUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

        // Check if IP filtering is enabled
        if (!securityProperties.getIpFiltering().isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Skip IP filtering for health check endpoints
        String requestUri = request.getRequestURI();
        if (isHealthCheckEndpoint(requestUri)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Get client IP address
        String clientIp = ipAddressUtil.getClientIpAddress(
            request.getHeader("X-Forwarded-For"),
            request.getHeader("X-Real-IP"),
            request.getRemoteAddr()
        );

        logger.info("IP Access Attempt - Client IP: {}, Request URI: {}", clientIp, requestUri);

        // Check if IP is allowed
        if (!ipAddressUtil.isIpAllowed(clientIp, securityProperties.getAllowedIps())) {
            logger.warn("Access denied for IP: {} attempting to access: {}", clientIp, requestUri);

            // Return 403 Forbidden with JSON response
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json");
            response.getWriter().write(String.format(
                "{\"status\": 403, \"error\": \"Access Denied\", \"message\": \"Access denied from IP address: %s\", \"timestamp\": \"%s\"}",
                clientIp, java.time.Instant.now().toString()
            ));
            return;
        }

        logger.debug("IP {} allowed - proceeding with request", clientIp);
        filterChain.doFilter(request, response);
    }

    /**
     * Check if the request is for a health check endpoint that should bypass IP filtering
     */
    private boolean isHealthCheckEndpoint(String requestUri) {
        return requestUri != null && (
            requestUri.contains("/healthcheck") ||
            requestUri.contains("/actuator/health") ||
            requestUri.contains("/health")
        );
    }
}
