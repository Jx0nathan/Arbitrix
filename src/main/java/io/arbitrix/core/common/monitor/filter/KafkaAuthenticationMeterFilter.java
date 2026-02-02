package io.arbitrix.core.common.monitor.filter;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.MeterFilterReply;

/**
 * Meter filter to ignore Kafka authentication metrics
 */
public class KafkaAuthenticationMeterFilter implements MeterFilter {

    @Override
    public MeterFilterReply accept(Meter.Id id) {
        // Ignore Kafka authentication related metrics
        if (id.getName().contains("kafka") && id.getName().contains("auth")) {
            return MeterFilterReply.DENY;
        }
        return MeterFilterReply.NEUTRAL;
    }
}
