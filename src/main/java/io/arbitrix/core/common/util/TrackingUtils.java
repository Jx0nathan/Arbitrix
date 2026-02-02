package io.arbitrix.core.common.util;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * Utility class for request tracking and tracing
 */
public final class TrackingUtils {

    private static final String TRACE_ID_KEY = "traceId";

    private TrackingUtils() {
    }

    /**
     * Generate a new UUID for tracking
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Save trace ID to MDC for logging
     */
    public static void saveTrace(String traceId) {
        MDC.put(TRACE_ID_KEY, traceId);
    }

    /**
     * Get current trace ID from MDC
     */
    public static String getTrace() {
        return MDC.get(TRACE_ID_KEY);
    }

    /**
     * Clear trace ID from MDC
     */
    public static void clearTrace() {
        MDC.remove(TRACE_ID_KEY);
    }
}
