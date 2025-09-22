package kaiquebt.dev.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class RequestContextService {

    /**
     * Get the current HttpServletRequest
     */
    public HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new IllegalStateException("No current HTTP request");
        }
        return attributes.getRequest();
    }
    
    /**
     * Extract client IP address from request, handling proxies and load balancers
     */
    public String getClientIp() {
        HttpServletRequest request = getCurrentRequest();
        String[] headersToCheck = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
        };
        
        for (String header : headersToCheck) {
            String ipAddress = request.getHeader(header);
            if (ipAddress != null && !ipAddress.isEmpty() && !"unknown".equalsIgnoreCase(ipAddress)) {
                if (ipAddress.contains(",")) {
                    ipAddress = ipAddress.split(",")[0].trim();
                }
                return ipAddress;
            }
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * Get User-Agent header from current request
     */
    public String getUserAgent() {
        return getCurrentRequest().getHeader("User-Agent");
    }
    
    /**
     * Get request method (GET, POST, etc.)
     */
    public String getRequestMethod() {
        return getCurrentRequest().getMethod();
    }
    
    /**
     * Get request URL
     */
    public String getRequestUrl() {
        return getCurrentRequest().getRequestURL().toString();
    }
    
    /**
     * Check if request is from localhost
     */
    public boolean isLocalRequest() {
        String ip = getClientIp();
        return "127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip) || "localhost".equals(ip);
    }
}