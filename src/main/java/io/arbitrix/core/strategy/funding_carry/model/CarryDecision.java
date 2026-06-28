package io.arbitrix.core.strategy.funding_carry.model;

import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * The decision engine's verdict for one (exchange, symbol) on one scan.
 *
 * @author jonathan.ji
 */
@Getter
@ToString
public class CarryDecision {

    public enum Action {
        /** Flat and conditions are favourable: open a new carry position. */
        OPEN,
        /** Holding but conditions have deteriorated: unwind the position. */
        CLOSE,
        /** Holding and conditions remain acceptable: do nothing. */
        HOLD,
        /** Flat and conditions are not favourable: do nothing. */
        SKIP
    }

    private final Action action;
    private final String exchange;
    private final String symbol;
    private final BigDecimal annualizedFunding;
    private final BigDecimal basis;
    private final String reason;

    private CarryDecision(Action action, String exchange, String symbol,
                          BigDecimal annualizedFunding, BigDecimal basis, String reason) {
        this.action = action;
        this.exchange = exchange;
        this.symbol = symbol;
        this.annualizedFunding = annualizedFunding;
        this.basis = basis;
        this.reason = reason;
    }

    public static CarryDecision of(Action action, FundingSnapshot snapshot, BigDecimal annualizedFunding, String reason) {
        return new CarryDecision(action, snapshot.getExchange(), snapshot.getSymbol(),
                annualizedFunding, snapshot.basis(), reason);
    }
}
