package io.arbitrix.core.strategy.funding_carry.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * An open (or closed) cash-and-carry position: spot long + perpetual short on the same exchange.
 *
 * @author jonathan.ji
 */
@Data
@Builder
public class CarryPosition {

    private String exchange;
    private String symbol;

    /** Spot base-coin quantity held long. */
    private BigDecimal spotQty;

    /** Perpetual base-coin quantity; negative means short. */
    private BigDecimal perpQty;

    private BigDecimal entrySpotPrice;
    private BigDecimal entryPerpPrice;

    /** Quote-currency (USDT) notional of the position. */
    private BigDecimal notionalUsdt;

    private long openTimeMs;

    /** Funding collected so far on this position (USDT). */
    @Builder.Default
    private BigDecimal accumulatedFundingUsdt = BigDecimal.ZERO;

    /** Latest known perp-leg margin ratio; nullable until the gateway/position manager reports it. */
    private BigDecimal marginRatio;

    @Builder.Default
    private boolean open = true;

    public String key() {
        return exchange + "_" + symbol;
    }
}
