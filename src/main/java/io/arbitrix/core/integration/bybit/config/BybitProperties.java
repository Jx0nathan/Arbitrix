package io.arbitrix.core.integration.bybit.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Data
@Component
@ConfigurationProperties(prefix = "bybit")
public class BybitProperties {
    private String category;
    private String apiKey;
    private String secretKey;
    private String restBaseUrl;
    private String wsPrivateBaseUrl;
    private String wsPublicBaseUrl;
    private String wsFuturePublicBaseUrl;
}
