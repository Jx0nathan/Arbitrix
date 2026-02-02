package io.arbitrix.core.common.monitor.filter;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.MeterFilterReply;

/**
 * Meter filter to ignore H2C upgrade metrics
 */
public class H2CUpgradeIgnoreMeterFilter implements MeterFilter {

    @Override
    public MeterFilterReply accept(Meter.Id id) {
        // Ignore H2C upgrade related metrics
        if (id.getName().contains("h2c") || id.getName().contains("upgrade")) {
            return MeterFilterReply.DENY;
        }
        return MeterFilterReply.NEUTRAL;
    }
}
