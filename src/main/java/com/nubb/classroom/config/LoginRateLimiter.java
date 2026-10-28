package com.nubb.classroom.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginRateLimiter {

    private static final int MAX_FAILED_ATTEMPTS = 20;
    private static final long LOCKOUT_DURATION_MS = 2L * 60 * 1000; // 2 minutes

    private static class AttemptInfo {
        int count;
        long lockoutUntil;

        AttemptInfo(int count, long lockoutUntil) {
            this.count = count;
            this.lockoutUntil = lockoutUntil;
        }
    }

    private final ConcurrentHashMap<String, AttemptInfo> attemptsMap = new ConcurrentHashMap<>();

    public String getClientIp(HttpServletRequest request) {
        String cfIp = request.getHeader("CF-Connecting-IP");
        if (cfIp != null && !cfIp.trim().isEmpty()) {
            return cfIp.trim();
        }
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.trim().isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.trim().isEmpty()) {
            return xRealIp.trim();
        }
        return request.getRemoteAddr();
    }

    public boolean isBlocked(String clientIp) {
        AttemptInfo info = attemptsMap.get(clientIp);
        if (info == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        if (now < info.lockoutUntil) {
            return true;
        }
        if (info.lockoutUntil > 0 && now >= info.lockoutUntil) {
            // Lockout expired, reset
            attemptsMap.remove(clientIp);
            return false;
        }
        return false;
    }

    public long getRemainingLockoutMinutes(String clientIp) {
        AttemptInfo info = attemptsMap.get(clientIp);
        if (info == null) return 0;
        long diff = info.lockoutUntil - System.currentTimeMillis();
        if (diff <= 0) return 0;
        return (diff / 60000) + 1;
    }

    public void recordFailedAttempt(String clientIp) {
        long now = System.currentTimeMillis();
        attemptsMap.compute(clientIp, (ip, info) -> {
            if (info == null) {
                return new AttemptInfo(1, 0);
            }
            if (now >= info.lockoutUntil && info.lockoutUntil > 0) {
                return new AttemptInfo(1, 0);
            }
            int newCount = info.count + 1;
            long lockoutUntil = (newCount >= MAX_FAILED_ATTEMPTS) ? (now + LOCKOUT_DURATION_MS) : 0;
            return new AttemptInfo(newCount, lockoutUntil);
        });
    }

    public void recordSuccess(String clientIp) {
        attemptsMap.remove(clientIp);
    }
}
