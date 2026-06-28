package io.arbitrix.core.strategy.funding_carry.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * A point-in-time view of one (exchange, symbol): its current/predicted funding rate plus spot and perpetual prices.
 * Pure value object; the decision engine derives everything it needs from here.
 *
 * @author jonathan.ji
 */
@Data
@Builder
public class FundingSnapshot {

    private String exchange;
    private String symbol;

    /** Funding rate for the current settlement period, as a fraction (0.0001 = 0.01%). Positive => shorts get paid. */
    private BigDecimal fundingRate;

    /** Exchange-predicted next funding rate, if available. Preferred over {@link #fundingRate} when present. */
    private BigDecimal predictedFundingRate;

    /** Settlement periods per day (8h cadence => 3). */
    @Builder.Default
    private int periodsPerDay = 3;

    private BigDecimal spotPrice;
    private BigDecimal perpPrice;

    /** Epoch millis of the next funding settlement. */
    private long nextFundingTimeMs;

    public BigDecimal effectiveFundingRate() {
        if (predictedFundingRate != null) {
            return predictedFundingRate;
        }
        return fundingRate != null ? fundingRate : BigDecimal.ZERO;
    }

    /** Annualized funding as a fraction: rate * periodsPerDay * 365. */
    public BigDecimal annualizedFunding() {
        return effectiveFundingRate().multiply(BigDecimal.valueOf((long) periodsPerDay * 365L));
    }

    /** (perp - spot) / spot. Returns ZERO when prices are unusable. */
    public BigDecimal basis() {
        if (spotPrice == null || perpPrice == null || spotPrice.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return perpPrice.subtract(spotPrice).divide(spotPrice, 8, RoundingMode.HALF_UP);
    }

    public boolean hasValidPrices() {
        return spotPrice != null && perpPrice != null && spotPrice.signum() > 0 && perpPrice.signum() > 0;
    }
}
