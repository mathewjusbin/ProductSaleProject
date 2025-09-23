package com.sparksupport.product.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    private List<String> allowedIps;
    private IpFiltering ipFiltering = new IpFiltering();

    public List<String> getAllowedIps() {
        return allowedIps;
    }

    public void setAllowedIps(List<String> allowedIps) {
        this.allowedIps = allowedIps;
    }

    public IpFiltering getIpFiltering() {
        return ipFiltering;
    }

    public void setIpFiltering(IpFiltering ipFiltering) {
        this.ipFiltering = ipFiltering;
    }

    public static class IpFiltering {
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
