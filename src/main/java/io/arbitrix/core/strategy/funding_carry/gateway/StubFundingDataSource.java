package io.arbitrix.core.strategy.funding_carry.gateway;

import io.arbitrix.core.strategy.base.condition.ExecuteStrategyConditional;
import io.arbitrix.core.strategy.funding_carry.model.FundingSnapshot;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Placeholder {@link FundingDataSource} so the strategy boots before real per-exchange adapters exist.
 * It reports connected but returns an empty (zero-funding, no-price) snapshot, so the engine always SKIPs
 * and nothing trades. Provide a real {@code @Component} implementing {@link FundingDataSource} to replace it.
 *
 * @author jonathan.ji
 */
@Component
@Log4j2
@ExecuteStrategyConditional(executeStrategyName = "funding_carry")
@ConditionalOnMissingBean(FundingDataSource.class)
public class StubFundingDataSource implements FundingDataSource {

    public StubFundingDataSource() {
        log.warn("StubFundingDataSource active — no real funding data. Implement a FundingDataSource adapter to trade.");
    }

    @Override
    public FundingSnapshot fetch(String exchange, String symbol) {
        return FundingSnapshot.builder()
                .exchange(exchange)
                .symbol(symbol)
                .fundingRate(BigDecimal.ZERO)
                .build();
    }

    @Override
    public boolean isConnected(String exchange) {
        return true;
    }
}
