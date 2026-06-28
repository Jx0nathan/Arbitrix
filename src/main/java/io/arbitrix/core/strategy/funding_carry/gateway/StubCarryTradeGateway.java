package io.arbitrix.core.strategy.funding_carry.gateway;

import io.arbitrix.core.strategy.base.condition.ExecuteStrategyConditional;
import io.arbitrix.core.strategy.funding_carry.model.CarryPosition;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Placeholder {@link CarryTradeGateway} that refuses to trade. It exists so the context wires up and paper-trade
 * mode works end-to-end; in live mode it fails loudly rather than sending half a hedge. Replace with a real adapter.
 *
 * @author jonathan.ji
 */
@Component
@Log4j2
@ExecuteStrategyConditional(executeStrategyName = "funding_carry")
@ConditionalOnMissingBean(CarryTradeGateway.class)
public class StubCarryTradeGateway implements CarryTradeGateway {

    public StubCarryTradeGateway() {
        log.warn("StubCarryTradeGateway active — live trading disabled. Implement a CarryTradeGateway adapter.");
    }

    @Override
    public OpenResult openCarry(String exchange, String symbol, BigDecimal notionalUsdt,
                                BigDecimal spotPrice, BigDecimal perpPrice) {
        return OpenResult.fail("no real CarryTradeGateway configured (stub refuses to trade)");
    }

    @Override
    public boolean closeCarry(CarryPosition position) {
        log.error("StubCarryTradeGateway.closeCarry called for {} — cannot close, no real adapter", position.key());
        return false;
    }
}
