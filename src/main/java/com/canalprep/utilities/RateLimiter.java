package com.canalprep.utilities;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple fixed-window rate limiter keyed by an identifier (e.g., IP address).
 * Not intended for distributed environments; suitable for single-node deployments.
 */
public class RateLimiter {
    private static class Window {
        long windowStartMs;
        int count;
    }

    private static final Map<String, Window> windows = new ConcurrentHashMap<>();

    /**
     * Check and consume a request for the given key.
     * @param key unique key (e.g., route + ip)
     * @param maxRequests max requests per window
     * @param windowMs window length in milliseconds
     * @return true if allowed; false if rate-limited
     */
    public static boolean checkAndConsume(String key, int maxRequests, long windowMs) {
        long now = System.currentTimeMillis();
        Window w = windows.computeIfAbsent(key, k -> {
            Window nw = new Window();
            nw.windowStartMs = now;
            nw.count = 0;
            return nw;
        });

        synchronized (w) {
            if (now - w.windowStartMs >= windowMs) {
                w.windowStartMs = now;
                w.count = 0;
            }
            if (w.count >= maxRequests) {
                return false;
            }
            w.count++;
            return true;
        }
    }
}