package com.yabo.addressbook.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 3;
    private static final long LOCK_DURATION_MS = 5 * 60 * 1000L; // 5 minutes

    private final Map<String, AttemptInfo> attemptsCache = new ConcurrentHashMap<>();

    /**
     * Record a failed login attempt for the given IP.
     */
    public void loginFailed(String ip) {
        attemptsCache.compute(ip, (key, info) -> {
            if (info == null) {
                info = new AttemptInfo();
            }
            info.attempts++;
            info.lastAttemptTime = System.currentTimeMillis();
            if (info.attempts >= MAX_ATTEMPTS) {
                info.lockTime = System.currentTimeMillis();
            }
            return info;
        });
    }

    /**
     * Check if the given IP is currently locked.
     */
    public boolean isLocked(String ip) {
        AttemptInfo info = attemptsCache.get(ip);
        if (info == null || info.attempts < MAX_ATTEMPTS) {
            return false;
        }
        // Check if lock duration has expired
        if (System.currentTimeMillis() - info.lockTime > LOCK_DURATION_MS) {
            attemptsCache.remove(ip);
            return false;
        }
        return true;
    }

    /**
     * Get the remaining attempts before lock for the given IP.
     */
    public int getRemainingAttempts(String ip) {
        AttemptInfo info = attemptsCache.get(ip);
        if (info == null) return MAX_ATTEMPTS;
        return Math.max(0, MAX_ATTEMPTS - info.attempts);
    }

    /**
     * Get the remaining lock time in seconds for the given IP.
     */
    public long getLockTimeRemainingSeconds(String ip) {
        AttemptInfo info = attemptsCache.get(ip);
        if (info == null || info.attempts < MAX_ATTEMPTS) return 0;
        long remaining = LOCK_DURATION_MS - (System.currentTimeMillis() - info.lockTime);
        return Math.max(0, remaining / 1000);
    }

    /**
     * Clear attempts record on successful login.
     */
    public void loginSucceeded(String ip) {
        attemptsCache.remove(ip);
    }

    private static class AttemptInfo {
        int attempts = 0;
        long lastAttemptTime = 0;
        long lockTime = 0;
    }
}