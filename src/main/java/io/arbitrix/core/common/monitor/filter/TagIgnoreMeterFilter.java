package io.arbitrix.core.common.monitor.filter;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.MeterFilterReply;

/**
 * Meter filter to ignore certain tags
 */
public class TagIgnoreMeterFilter implements MeterFilter {

    @Override
    public MeterFilterReply accept(Meter.Id id) {
        return MeterFilterReply.NEUTRAL;
    }
}
