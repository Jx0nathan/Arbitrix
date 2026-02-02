package io.arbitrix.core.common.monitor.config;

import io.arbitrix.core.common.enums.ExchangeNameEnum;
import io.arbitrix.core.common.monitor.filter.H2CUpgradeIgnoreMeterFilter;
import io.arbitrix.core.common.monitor.filter.KafkaAuthenticationMeterFilter;
import io.arbitrix.core.common.monitor.filter.TagIgnoreMeterFilter;
import io.arbitrix.core.utils.ApplicationExecuteStrategyUtil;
import io.arbitrix.core.utils.ExchangeMarketOpenUtilV2;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Log4j2
public class ArbitrixMeterRegistryCustomizer implements MeterRegistryCustomizer<MeterRegistry> {
    private final ExchangeMarketOpenUtilV2 exchangeMarketOpenUtilV2;
    private final ApplicationExecuteStrategyUtil applicationExecuteStrategyUtil;
    private final MonitorProperties monitorProperties;
    private List<Tag> tags;

    public ArbitrixMeterRegistryCustomizer(ExchangeMarketOpenUtilV2 exchangeMarketOpenUtilV2,
                                            ApplicationExecuteStrategyUtil applicationExecuteStrategyUtil,
                                            MonitorProperties monitorProperties) {
        this.exchangeMarketOpenUtilV2 = exchangeMarketOpenUtilV2;
        this.applicationExecuteStrategyUtil = applicationExecuteStrategyUtil;
        this.monitorProperties = monitorProperties;
        this.tags = List.of(Tag.of("strategy", this.generateStrategy()));
    }

    private String generateStrategy() {
        ExchangeNameEnum exchange = this.exchangeMarketOpenUtilV2.getExchange();
        String symbolPairs = String.join("-", this.exchangeMarketOpenUtilV2.getSymbolPairs(exchange));
        return String.join("-",
                exchange.getValue(),
                symbolPairs,
                applicationExecuteStrategyUtil.getApplicationExecuteStrategyName(),
                this.monitorProperties.getStrategyAlias());
    }

    @Override
    public void customize(MeterRegistry registry) {
        List<Tag> allTags = new ArrayList<>(this.getArbitrixCommonTags());
        registry.config()
                .commonTags(allTags)
                .meterFilter(new TagIgnoreMeterFilter())
                .meterFilter(new H2CUpgradeIgnoreMeterFilter())
                .meterFilter(new KafkaAuthenticationMeterFilter());
    }

    public List<Tag> getArbitrixCommonTags() {
        if (this.tags == null) {
            this.tags = Arrays.asList(
                    Tag.of("strategy", this.generateStrategy()),
                    Tag.of("global_exchange_name", this.exchangeMarketOpenUtilV2.getExchange().getValue())
            );
        }
        return this.tags;
    }
}
