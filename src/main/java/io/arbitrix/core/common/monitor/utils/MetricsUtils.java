package io.arbitrix.core.common.monitor.utils;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for recording metrics using Micrometer
 */
@Slf4j
public final class MetricsUtils {

    private static final MeterRegistry registry = Metrics.globalRegistry;

    private MetricsUtils() {
    }

    /**
     * Record a timing metric with default percentiles
     *
     * @param name        metric name
     * @param description metric description
     * @param duration    the duration to record
     * @param tags        key-value pairs of tags
     */
    public static void recordTimeDefaultPercentiles(String name, String description, Duration duration, String... tags) {
        try {
            Timer timer = Timer.builder(name)
                    .description(description)
                    .tags(tags)
                    .publishPercentiles(0.5, 0.75, 0.95, 0.99)
                    .register(registry);
            timer.record(duration);
        } catch (Exception e) {
            log.warn("Failed to record metric: {}", name, e);
        }
    }

    /**
     * Record a timing metric
     *
     * @param name     metric name
     * @param duration duration in milliseconds
     * @param tags     key-value pairs of tags
     */
    public static void recordTime(String name, long duration, String... tags) {
        try {
            Timer timer = Timer.builder(name)
                    .tags(tags)
                    .register(registry);
            timer.record(duration, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.warn("Failed to record metric: {}", name, e);
        }
    }

    /**
     * Increment a counter
     *
     * @param name metric name
     * @param tags key-value pairs of tags
     */
    public static void incrementCounter(String name, String... tags) {
        try {
            Metrics.counter(name, tags).increment();
        } catch (Exception e) {
            log.warn("Failed to increment counter: {}", name, e);
        }
    }

    /**
     * Record a gauge value
     *
     * @param name  metric name
     * @param value the value to record
     * @param tags  key-value pairs of tags
     */
    public static void recordGauge(String name, double value, String... tags) {
        try {
            Metrics.gauge(name, io.micrometer.core.instrument.Tags.of(tags), value);
        } catch (Exception e) {
            log.warn("Failed to record gauge: {}", name, e);
        }
    }

    /**
     * Count metric with multiple tags
     *
     * @param name metric name
     * @param tags key-value pairs of tags
     */
    public static void count(String name, String... tags) {
        try {
            Metrics.counter(name, tags).increment();
        } catch (Exception e) {
            log.warn("Failed to count metric: {}", name, e);
        }
    }

    /**
     * Count metric with description and tags
     *
     * @param name        metric name
     * @param description metric description (ignored, for API compatibility)
     * @param tags        key-value pairs of tags
     */
    public static void count(String name, String description, String[] tags) {
        try {
            Metrics.counter(name, tags).increment();
        } catch (Exception e) {
            log.warn("Failed to count metric: {}", name, e);
        }
    }
}
