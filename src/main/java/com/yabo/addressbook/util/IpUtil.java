package com.yabo.addressbook.util;

import jakarta.servlet.http.HttpServletRequest;

public class IpUtil {

    private IpUtil() {}

    /**
     * Extract client IP from request, checking X-Forwarded-For and X-Real-IP headers.
     */
    public static String getClientIP(HttpServletRequest request) {
        // X-Forwarded-For: client, proxy1, proxy2
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0].trim();
        }

        // X-Real-IP: used by some reverse proxies
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isEmpty()) {
            return realIp.trim();
        }

        return request.getRemoteAddr();
    }
}