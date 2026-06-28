package io.arbitrix.funding_carry;

import io.arbitrix.core.strategy.funding_carry.config.FundingCarryProperties;
import io.arbitrix.core.strategy.funding_carry.model.CarryPosition;
import io.arbitrix.core.strategy.funding_carry.risk.CarryRiskGuard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CarryRiskGuardTest {

    private CarryRiskGuard guard;
    private FundingCarryProperties props;

    @BeforeEach
    public void setUp() {
        guard = new CarryRiskGuard();
        props = new FundingCarryProperties();
        props.setMaxTotalNotionalUsdt(new BigDecimal("1000"));
        props.setMaxDailyLossUsdt(new BigDecimal("100"));
        props.setMinMarginRatioBuffer(new BigDecimal("0.5"));
        props.setKillSwitchEnabled(true);
    }

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    @Test
    public void canOpen_allowsWhenHealthy() {
        assertTrue(guard.canOpen(ZERO, new BigDecimal("500"), ZERO, true, props).isFlag());
    }

    @Test
    public void canOpen_deniesWhenTotalNotionalExceeded() {
        assertFalse(guard.canOpen(new BigDecimal("800"), new BigDecimal("500"), ZERO, true, props).isFlag());
    }

    @Test
    public void canOpen_deniesWhenDisconnected() {
        assertFalse(guard.canOpen(ZERO, new BigDecimal("500"), ZERO, false, props).isFlag());
    }

    @Test
    public void canOpen_deniesWhenDailyLossLimitHit() {
        assertFalse(guard.canOpen(ZERO, new BigDecimal("500"), new BigDecimal("-150"), true, props).isFlag());
    }

    @Test
    public void forceClose_whenMarginBelowBuffer() {
        CarryPosition pos = CarryPosition.builder().exchange("OKX").symbol("BTCUSDT")
                .marginRatio(new BigDecimal("0.3")).open(true).build();
        assertTrue(guard.shouldForceClose(pos, null, ZERO, props).isFlag());
    }

    @Test
    public void forceClose_whenDailyLossKillSwitch() {
        CarryPosition pos = CarryPosition.builder().exchange("OKX").symbol("BTCUSDT")
                .marginRatio(new BigDecimal("1.0")).open(true).build();
        CarryRiskGuard.Verdict v = guard.shouldForceClose(pos, null, new BigDecimal("-150"), props);
        assertTrue(v.isFlag());
        assertTrue(v.getReason().contains("kill switch"));
    }

    @Test
    public void noForceClose_whenHealthy() {
        CarryPosition pos = CarryPosition.builder().exchange("OKX").symbol("BTCUSDT")
                .marginRatio(new BigDecimal("1.0")).open(true).build();
        assertFalse(guard.shouldForceClose(pos, null, ZERO, props).isFlag());
    }
}
