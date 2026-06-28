package io.arbitrix.core.strategy.funding_carry.config;

import io.arbitrix.core.strategy.base.condition.ExecuteStrategyConditional;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for the same-exchange cash-and-carry (spot long + perpetual short) funding strategy.
 * <p>
 * Bound from {@code strategy.funding-carry.*}. All values have safe defaults; the most important one is
 * {@link #paperTrade} which defaults to {@code true} so the strategy never sends real orders unless explicitly enabled.
 *
 * @author jonathan.ji
 */
@Data
@Component
@ConfigurationProperties(prefix = "strategy.funding-carry")
@ExecuteStrategyConditional(executeStrategyName = "funding_carry")
public class FundingCarryProperties {

    /** When true, no real orders are placed; decisions are simulated and logged. Default true for safety. */
    private boolean paperTrade = true;

    /** How often the scanner evaluates every (exchange, symbol). */
    private long scanIntervalMs = 60_000L;

    /** Exchanges to run cash-and-carry on. Note: Bitget has no perpetual line in this codebase. */
    private List<String> exchanges = new ArrayList<>(List.of("OKX"));

    /** Symbols (arbitrix format, e.g. BTCUSDT) to monitor. */
    private List<String> symbols = new ArrayList<>(List.of("BTCUSDT"));

    /** Open a position only when annualized funding (as a fraction, 0.15 = 15%) is at least this. */
    private BigDecimal openAnnualizedThreshold = new BigDecimal("0.15");

    /** Close an open position when annualized funding drops below this (hysteresis vs the open threshold). */
    private BigDecimal closeAnnualizedThreshold = new BigDecimal("0.05");

    /** Maximum absolute (perp-spot)/spot basis allowed at entry (0.001 = 0.1%). Avoids entering on a rich perp. */
    private BigDecimal maxBasisToOpen = new BigDecimal("0.001");

    /** While holding, force-close if absolute basis blows out beyond this (0.005 = 0.5%). */
    private BigDecimal maxBasisToHold = new BigDecimal("0.005");

    /** Estimated round-trip transaction cost as a fraction of notional (4 fee legs + slippage). Used for sizing/reporting. */
    private BigDecimal roundTripCost = new BigDecimal("0.003");

    /** Notional (in quote currency, USDT) deployed per pair. */
    private BigDecimal notionalPerPairUsdt = new BigDecimal("500");

    /** Leverage used on the perpetual short leg. Keep low at small scale for liquidation safety. */
    private BigDecimal perpLeverage = new BigDecimal("2");

    /** Force-close a position if its perp-leg margin ratio falls below this buffer. */
    private BigDecimal minMarginRatioBuffer = new BigDecimal("0.5");

    /** Consecutive negative funding periods tolerated before exiting (reserved for forward-funding logic). */
    private int negativeFundingExitPeriods = 2;

    /** Hard cap on aggregate open notional across all pairs. */
    private BigDecimal maxTotalNotionalUsdt = new BigDecimal("2000");

    /** Daily realized loss (USDT, positive number) that trips the kill switch. */
    private BigDecimal maxDailyLossUsdt = new BigDecimal("100");

    /** When true, breaching {@link #maxDailyLossUsdt} closes everything and halts new opens. */
    private boolean killSwitchEnabled = true;
}
