package io.arbitrix.funding_carry;

import io.arbitrix.core.strategy.funding_carry.config.FundingCarryProperties;
import io.arbitrix.core.strategy.funding_carry.engine.FundingCarryDecisionEngine;
import io.arbitrix.core.strategy.funding_carry.model.CarryDecision;
import io.arbitrix.core.strategy.funding_carry.model.CarryPosition;
import io.arbitrix.core.strategy.funding_carry.model.FundingSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FundingCarryDecisionEngineTest {

    private FundingCarryDecisionEngine engine;
    private FundingCarryProperties props;

    @BeforeEach
    public void setUp() {
        engine = new FundingCarryDecisionEngine();
        props = new FundingCarryProperties();
        // open >= 15% annualized, close < 5%, entry basis <= 0.1%, hold basis <= 0.5%
    }

    /** 3 periods/day * 365 = 1095. rate 0.0002 => annualized 0.219 (>= 0.15). */
    private FundingSnapshot snapshot(String rate, String spot, String perp) {
        return FundingSnapshot.builder()
                .exchange("OKX").symbol("BTCUSDT")
                .fundingRate(new BigDecimal(rate))
                .spotPrice(new BigDecimal(spot))
                .perpPrice(new BigDecimal(perp))
                .build();
    }

    private CarryPosition openPosition() {
        return CarryPosition.builder().exchange("OKX").symbol("BTCUSDT")
                .notionalUsdt(new BigDecimal("500")).open(true).build();
    }

    @Test
    public void open_whenFundingHighAndBasisTight() {
        // basis = (100.05-100)/100 = 0.0005 <= 0.001
        CarryDecision d = engine.decide(snapshot("0.0002", "100", "100.05"), null, props);
        assertEquals(CarryDecision.Action.OPEN, d.getAction());
    }

    @Test
    public void skip_whenFundingTooLow() {
        // annualized = 0.00001 * 1095 = 0.01095 < 0.15
        CarryDecision d = engine.decide(snapshot("0.00001", "100", "100.05"), null, props);
        assertEquals(CarryDecision.Action.SKIP, d.getAction());
    }

    @Test
    public void skip_whenBasisTooWideAtEntry() {
        // basis = (101-100)/100 = 0.01 > 0.001 even though funding is high
        CarryDecision d = engine.decide(snapshot("0.0002", "100", "101"), null, props);
        assertEquals(CarryDecision.Action.SKIP, d.getAction());
    }

    @Test
    public void skip_whenPricesInvalid() {
        FundingSnapshot s = FundingSnapshot.builder()
                .exchange("OKX").symbol("BTCUSDT").fundingRate(new BigDecimal("0.0002"))
                .spotPrice(BigDecimal.ZERO).perpPrice(new BigDecimal("100")).build();
        assertEquals(CarryDecision.Action.SKIP, engine.decide(s, null, props).getAction());
    }

    @Test
    public void hold_whenHoldingAndFundingStillAttractive() {
        CarryDecision d = engine.decide(snapshot("0.0002", "100", "100.05"), openPosition(), props);
        assertEquals(CarryDecision.Action.HOLD, d.getAction());
    }

    @Test
    public void close_whenHoldingAndFundingDrops() {
        CarryDecision d = engine.decide(snapshot("0.00001", "100", "100.05"), openPosition(), props);
        assertEquals(CarryDecision.Action.CLOSE, d.getAction());
    }

    @Test
    public void close_whenHoldingAndFundingFlipsNegative() {
        CarryDecision d = engine.decide(snapshot("-0.0002", "100", "100.05"), openPosition(), props);
        assertEquals(CarryDecision.Action.CLOSE, d.getAction());
    }

    @Test
    public void close_whenHoldingAndBasisBlowsOut() {
        // basis = (100.6-100)/100 = 0.006 > 0.005 hold limit; funding still high
        CarryDecision d = engine.decide(snapshot("0.0002", "100", "100.6"), openPosition(), props);
        assertEquals(CarryDecision.Action.CLOSE, d.getAction());
    }
}
