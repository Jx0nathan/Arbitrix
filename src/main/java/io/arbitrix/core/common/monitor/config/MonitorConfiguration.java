package io.arbitrix.core.common.monitor.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.arbitrix.core.common.monitor.aop.PercentilesMetricsAspect;
import io.arbitrix.core.common.monitor.aop.RecordRestApiDurationAspect;
import io.arbitrix.core.common.monitor.httpclient.HttpClientPoolMetrics;
import io.arbitrix.core.utils.ApplicationExecuteStrategyUtil;
import io.arbitrix.core.utils.ExchangeMarketOpenUtilV2;

/**
 * @author mcx
 * @date 2023/10/30
 * @description
 */
@Configuration
public class MonitorConfiguration {

    @Bean
    public RecordRestApiDurationAspect recordRestApiDurationAspect() {
        return new RecordRestApiDurationAspect();
    }
    @Bean
    public PercentilesMetricsAspect percentilesMetricsAspect() {
        return new PercentilesMetricsAspect();
    }

    @Bean
    public ArbitrixMeterRegistryCustomizer arbitrixMeterRegistryCustomizer(ExchangeMarketOpenUtilV2 exchangeMarketOpenUtilV2, MonitorProperties monitorProperties, ApplicationExecuteStrategyUtil applicationExecuteStrategyUtil) {
        return new ArbitrixMeterRegistryCustomizer(exchangeMarketOpenUtilV2, applicationExecuteStrategyUtil, monitorProperties);
    }

    @Bean
    @ConditionalOnProperty(value = "feign.httpclient.enabled", havingValue = "true")
    public HttpClientPoolMetrics httpClientPoolMetrics() {
        return new HttpClientPoolMetrics();
    }
}
