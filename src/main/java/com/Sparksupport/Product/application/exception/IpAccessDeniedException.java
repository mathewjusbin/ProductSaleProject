package com.sparksupport.product.application.exception;

public class IpAccessDeniedException extends RuntimeException {
    private final String clientIp;
    private final String requestUri;

    public IpAccessDeniedException(String clientIp, String requestUri) {
        super(String.format("Access denied from IP address: %s for URI: %s", clientIp, requestUri));
        this.clientIp = clientIp;
        this.requestUri = requestUri;
    }

    public String getClientIp() {
        return clientIp;
    }

    public String getRequestUri() {
        return requestUri;
    }
}
