package com.sparksupport.product.application.util;

import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

@Component
public class IpAddressUtil {

    /**
     * Check if the given IP address is allowed based on the configured allowed IPs list
     * Supports both individual IPs and CIDR notation (e.g., 192.168.1.0/24)
     */
    public boolean isIpAllowed(String clientIp, List<String> allowedIps) {
        if (clientIp == null || allowedIps == null || allowedIps.isEmpty()) {
            return false;
        }

        // Handle localhost variations
        if (isLocalhost(clientIp)) {
            clientIp = "127.0.0.1";
        }

        for (String allowedIp : allowedIps) {
            if (matchesIpOrCidr(clientIp, allowedIp.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if an IP matches a single IP or CIDR notation
     */
    private boolean matchesIpOrCidr(String clientIp, String allowedIp) {
        try {
            if (allowedIp.contains("/")) {
                // CIDR notation (e.g., 192.168.1.0/24)
                return isInCidrRange(clientIp, allowedIp);
            } else {
                // Single IP address
                return clientIp.equals(allowedIp);
            }
        } catch (Exception e) {
            // If there's any error in IP parsing, deny access for security
            return false;
        }
    }

    /**
     * Check if an IP is in a CIDR range
     */
    private boolean isInCidrRange(String clientIp, String cidr) {
        try {
            String[] parts = cidr.split("/");
            String networkIp = parts[0];
            int prefixLength = Integer.parseInt(parts[1]);

            InetAddress clientAddr = InetAddress.getByName(clientIp);
            InetAddress networkAddr = InetAddress.getByName(networkIp);

            byte[] clientBytes = clientAddr.getAddress();
            byte[] networkBytes = networkAddr.getAddress();

            if (clientBytes.length != networkBytes.length) {
                return false; // IPv4 vs IPv6 mismatch
            }

            int bytesToCheck = prefixLength / 8;
            int bitsToCheck = prefixLength % 8;

            // Check full bytes
            for (int i = 0; i < bytesToCheck; i++) {
                if (clientBytes[i] != networkBytes[i]) {
                    return false;
                }
            }

            // Check remaining bits in the partial byte
            if (bitsToCheck > 0) {
                int mask = 0xFF << (8 - bitsToCheck);
                return (clientBytes[bytesToCheck] & mask) == (networkBytes[bytesToCheck] & mask);
            }

            return true;
        } catch (UnknownHostException | NumberFormatException e) {
            return false;
        }
    }

    /**
     * Check if the IP represents localhost
     */
    private boolean isLocalhost(String ip) {
        return "0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip) || "localhost".equals(ip);
    }

    /**
     * Extract the real client IP address from the request
     * Considers proxy headers like X-Forwarded-For, X-Real-IP
     */
    public String getClientIpAddress(String xForwardedFor, String xRealIp, String remoteAddr) {
        // Check X-Forwarded-For header (most common for proxies/load balancers)
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            // X-Forwarded-For can contain multiple IPs, take the first one
            return xForwardedFor.split(",")[0].trim();
        }

        // Check X-Real-IP header
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        // Fall back to remote address
        return remoteAddr;
    }
}
