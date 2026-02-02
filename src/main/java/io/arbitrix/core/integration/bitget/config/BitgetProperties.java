package io.arbitrix.core.integration.bitget.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Data
@Component
@ConfigurationProperties(prefix = "bitget")
public class BitgetProperties {
    private String apiKey;
    private String secretKey;
    private String passPhrase;
    private String restBaseUrl;
    private String wsBaseUrl;
}
