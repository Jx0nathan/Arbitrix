package io.arbitrix.core.strategy.funding_carry.risk;

import io.arbitrix.core.strategy.funding_carry.config.FundingCarryProperties;
import io.arbitrix.core.strategy.funding_carry.model.CarryPosition;
import io.arbitrix.core.strategy.funding_carry.model.FundingSnapshot;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Independent risk layer evaluated before opening and on every held position. This is where the
 * (otherwise unwired) max-position / max-daily-loss / kill-switch controls actually live.
 *
 * @author jonathan.ji
 */
@Component
public class CarryRiskGuard {

    /** Result of a risk check. {@code flag} meaning is documented per method. */
    @Getter
    @AllArgsConstructor
    public static class Verdict {
        private final boolean flag;
        private final String reason;

        public static Verdict of(boolean flag, String reason) {
            return new Verdict(flag, reason);
        }
    }

    /**
     * Whether a new position may be opened. {@code flag == true} means allowed.
     */
    public Verdict canOpen(BigDecimal currentTotalNotional, BigDecimal addNotional,
                           BigDecimal dailyPnl, boolean allConnected, FundingCarryProperties props) {
        if (props.isKillSwitchEnabled() && dailyPnl.compareTo(props.getMaxDailyLossUsdt().negate()) <= 0) {
            return Verdict.of(false, "daily loss limit hit (pnl=" + dailyPnl + ")");
        }
        if (!allConnected) {
            return Verdict.of(false, "exchange connectivity not healthy");
        }
        BigDecimal projected = currentTotalNotional.add(addNotional);
        if (projected.compareTo(props.getMaxTotalNotionalUsdt()) > 0) {
            return Verdict.of(false, "max total notional exceeded (" + projected + " > " + props.getMaxTotalNotionalUsdt() + ")");
        }
        return Verdict.of(true, "ok");
    }

    /**
     * Whether a held position must be force-closed. {@code flag == true} means close now.
     */
    public Verdict shouldForceClose(CarryPosition position, FundingSnapshot snapshot,
                                    BigDecimal dailyPnl, FundingCarryProperties props) {
        if (props.isKillSwitchEnabled() && dailyPnl.compareTo(props.getMaxDailyLossUsdt().negate()) <= 0) {
            return Verdict.of(true, "kill switch: daily loss limit hit (pnl=" + dailyPnl + ")");
        }
        if (position.getMarginRatio() != null
                && position.getMarginRatio().compareTo(props.getMinMarginRatioBuffer()) < 0) {
            return Verdict.of(true, "perp margin ratio " + position.getMarginRatio()
                    + " below buffer " + props.getMinMarginRatioBuffer());
        }
        return Verdict.of(false, "within risk limits");
    }
}
