package io.arbitrix.core.strategy.funding_carry.exec;

import io.arbitrix.core.strategy.base.condition.ExecuteStrategyConditional;
import io.arbitrix.core.strategy.funding_carry.config.FundingCarryProperties;
import io.arbitrix.core.strategy.funding_carry.gateway.CarryTradeGateway;
import io.arbitrix.core.strategy.funding_carry.model.CarryPosition;
import io.arbitrix.core.strategy.funding_carry.model.FundingSnapshot;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Bridges decisions to execution. In paper-trade mode it simulates fills and builds the resulting
 * position locally; in live mode it delegates to the {@link CarryTradeGateway} (which owns the atomic
 * two-leg open/rollback).
 *
 * @author jonathan.ji
 */
@Component
@Log4j2
@ExecuteStrategyConditional(executeStrategyName = "funding_carry")
public class FundingCarryExecutor {

    private final FundingCarryProperties props;
    private final CarryTradeGateway gateway;

    public FundingCarryExecutor(FundingCarryProperties props, CarryTradeGateway gateway) {
        this.props = props;
        this.gateway = gateway;
    }

    public CarryTradeGateway.OpenResult open(FundingSnapshot snapshot, BigDecimal notionalUsdt) {
        if (props.isPaperTrade()) {
            CarryPosition position = CarryPosition.builder()
                    .exchange(snapshot.getExchange())
                    .symbol(snapshot.getSymbol())
                    .notionalUsdt(notionalUsdt)
                    .entrySpotPrice(snapshot.getSpotPrice())
                    .entryPerpPrice(snapshot.getPerpPrice())
                    .spotQty(divide(notionalUsdt, snapshot.getSpotPrice()))
                    .perpQty(divide(notionalUsdt, snapshot.getPerpPrice()).negate())
                    .openTimeMs(System.currentTimeMillis())
                    .build();
            log.info("[PAPER] open carry {} notional={} spot={} perp={}",
                    position.key(), notionalUsdt, snapshot.getSpotPrice(), snapshot.getPerpPrice());
            return CarryTradeGateway.OpenResult.ok(position);
        }
        return gateway.openCarry(snapshot.getExchange(), snapshot.getSymbol(), notionalUsdt,
                snapshot.getSpotPrice(), snapshot.getPerpPrice());
    }

    public boolean close(CarryPosition position) {
        if (props.isPaperTrade()) {
            log.info("[PAPER] close carry {}", position.key());
            return true;
        }
        return gateway.closeCarry(position);
    }

    private static BigDecimal divide(BigDecimal notional, BigDecimal price) {
        if (notional == null || price == null || price.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return notional.divide(price, 8, RoundingMode.DOWN);
    }
}
