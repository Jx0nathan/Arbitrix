package io.arbitrix.core.common.monitor.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "monitor")
public class MonitorProperties {
    private String strategyAlias;
}
