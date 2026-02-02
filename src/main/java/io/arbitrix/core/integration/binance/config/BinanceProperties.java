package io.arbitrix.core.integration.binance.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author jonathan
 */
@Data
@Component
@ConfigurationProperties(prefix = "binance.mbx")
public class BinanceProperties {
    private String apiKey;
    private String secretKey;
    private String restBaseUrl;
    private String wsRestBaseUrl;
    private String wsBaseUrl;
}
