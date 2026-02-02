package io.arbitrix.core.integration.okx.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author jonathan
 */
@Data
@Component
@ConfigurationProperties(prefix = "okx.mbx")
public class OkxProperties {
    private String apiKey;
    private String secretKey;
    private String restBaseUrl;
    private String wsRestBaseUrl;
    private String wsBaseUrl;
    private boolean disableSslValidation = false;
    private int maxConnections = 500;
    private int maxConnectionsPerRoute = 200;
    private long timeToLive = 90;
    private TimeUnit timeToLiveUnit = TimeUnit.SECONDS;
    private int connectionTimerRepeat = 3000;
    private int connectionTimeout = 4000;
    private boolean followRedirects = true;
}
