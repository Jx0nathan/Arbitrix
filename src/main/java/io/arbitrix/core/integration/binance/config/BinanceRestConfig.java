package io.arbitrix.core.integration.binance.config;

import com.binance.connector.client.impl.SpotClientImpl;
import com.google.common.base.Preconditions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BinanceRestConfig {
    @Bean
    public SpotClientImpl createSpotClient(BinanceProperties properties) {
        Preconditions.checkState(null != properties.getApiKey(), "api key is null, please set value.");
        Preconditions.checkState(null != properties.getSecretKey(), "secretKey key is null, please set value.");
        Preconditions.checkState(null != properties.getRestBaseUrl(), "baseUrl key is null, please set value.");
        return new SpotClientImpl(properties.getApiKey(), properties.getSecretKey(), properties.getRestBaseUrl());
    }
}
