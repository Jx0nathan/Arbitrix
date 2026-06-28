package io.arbitrix.core.strategy.funding_carry.engine;

import io.arbitrix.core.strategy.funding_carry.config.FundingCarryProperties;
import io.arbitrix.core.strategy.funding_carry.model.CarryDecision;
import io.arbitrix.core.strategy.funding_carry.model.CarryPosition;
import io.arbitrix.core.strategy.funding_carry.model.FundingSnapshot;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Pure decision logic for same-exchange cash-and-carry.
 * <p>
 * Profit model (shorting the perp to collect funding):
 * <pre>
 *   annualized funding = effectiveRate * periodsPerDay * 365
 *   open  when flat and annualized >= openThreshold and |basis| <= maxBasisToOpen
 *   close when holding and (annualized < closeThreshold  or  |basis| > maxBasisToHold)
 * </pre>
 * The open/close thresholds form a hysteresis band so the strategy does not churn (and burn fees)
 * around the boundary. A funding flip to negative drives the annualized figure below the close
 * threshold, which closes the position.
 *
 * @author jonathan.ji
 */
@Component
public class FundingCarryDecisionEngine {

    public CarryDecision decide(FundingSnapshot snapshot, CarryPosition currentPosition, FundingCarryProperties props) {
        BigDecimal annualized = snapshot.annualizedFunding();
        BigDecimal basisAbs = snapshot.basis().abs();
        boolean holding = currentPosition != null && currentPosition.isOpen();

        if (holding) {
            if (annualized.compareTo(props.getCloseAnnualizedThreshold()) < 0) {
                return CarryDecision.of(CarryDecision.Action.CLOSE, snapshot, annualized,
                        "annualized funding " + annualized + " below close threshold " + props.getCloseAnnualizedThreshold());
            }
            if (basisAbs.compareTo(props.getMaxBasisToHold()) > 0) {
                return CarryDecision.of(CarryDecision.Action.CLOSE, snapshot, annualized,
                        "basis " + basisAbs + " exceeds hold limit " + props.getMaxBasisToHold());
            }
            return CarryDecision.of(CarryDecision.Action.HOLD, snapshot, annualized, "funding still attractive");
        }

        if (!snapshot.hasValidPrices()) {
            return CarryDecision.of(CarryDecision.Action.SKIP, snapshot, annualized, "invalid spot/perp prices");
        }
        if (annualized.compareTo(props.getOpenAnnualizedThreshold()) >= 0
                && basisAbs.compareTo(props.getMaxBasisToOpen()) <= 0) {
            return CarryDecision.of(CarryDecision.Action.OPEN, snapshot, annualized,
                    "annualized funding " + annualized + " >= open threshold and basis " + basisAbs + " within entry band");
        }
        return CarryDecision.of(CarryDecision.Action.SKIP, snapshot, annualized,
                "conditions not met (annualized=" + annualized + ", basis=" + basisAbs + ")");
    }
}
