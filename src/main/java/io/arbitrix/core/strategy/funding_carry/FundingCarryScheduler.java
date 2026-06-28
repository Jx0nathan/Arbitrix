package io.arbitrix.core.strategy.funding_carry;

import io.arbitrix.core.strategy.base.condition.ExecuteStrategyConditional;
import io.arbitrix.core.strategy.funding_carry.config.FundingCarryProperties;
import io.arbitrix.core.strategy.funding_carry.engine.FundingCarryDecisionEngine;
import io.arbitrix.core.strategy.funding_carry.exec.FundingCarryExecutor;
import io.arbitrix.core.strategy.funding_carry.gateway.CarryTradeGateway;
import io.arbitrix.core.strategy.funding_carry.gateway.FundingDataSource;
import io.arbitrix.core.strategy.funding_carry.model.CarryDecision;
import io.arbitrix.core.strategy.funding_carry.model.CarryPosition;
import io.arbitrix.core.strategy.funding_carry.model.FundingSnapshot;
import io.arbitrix.core.strategy.funding_carry.risk.CarryRiskGuard;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Low-frequency orchestrator for the funding cash-and-carry strategy. On each tick it walks every
 * configured (exchange, symbol): pulls a {@link FundingSnapshot}, runs the risk guard and decision engine,
 * and opens/holds/closes accordingly. Activated only when {@code application_execute_strategy_name=funding_carry}.
 *
 * @author jonathan.ji
 */
@Component
@Log4j2
@ExecuteStrategyConditional(executeStrategyName = "funding_carry")
public class FundingCarryScheduler {

    private final FundingCarryProperties props;
    private final FundingDataSource dataSource;
    private final FundingCarryDecisionEngine engine;
    private final CarryRiskGuard riskGuard;
    private final FundingCarryExecutor executor;

    private final Map<String, CarryPosition> positions = new ConcurrentHashMap<>();
    private volatile boolean halted = false;

    public FundingCarryScheduler(FundingCarryProperties props, FundingDataSource dataSource,
                                 FundingCarryDecisionEngine engine, CarryRiskGuard riskGuard,
                                 FundingCarryExecutor executor) {
        this.props = props;
        this.dataSource = dataSource;
        this.engine = engine;
        this.riskGuard = riskGuard;
        this.executor = executor;
        log.info("FundingCarryScheduler initialized. paperTrade={}, exchanges={}, symbols={}",
                props.isPaperTrade(), props.getExchanges(), props.getSymbols());
    }

    @Scheduled(fixedDelayString = "${strategy.funding-carry.scan-interval-ms:60000}", initialDelay = 10_000L)
    public void scan() {
        if (halted) {
            log.warn("FundingCarryScheduler halted by kill switch — skipping scan");
            return;
        }
        try {
            doScan();
        } catch (Exception e) {
            log.error("FundingCarryScheduler.scan error", e);
        }
    }

    private void doScan() {
        BigDecimal dailyPnl = computeDailyPnl();

        for (String exchange : props.getExchanges()) {
            if (!dataSource.isConnected(exchange)) {
                log.debug("FundingCarryScheduler: {} not connected, skipping", exchange);
                continue;
            }
            for (String symbol : props.getSymbols()) {
                FundingSnapshot snapshot = dataSource.fetch(exchange, symbol);
                if (snapshot == null) {
                    continue;
                }
                String key = exchange + "_" + symbol;
                CarryPosition position = positions.get(key);

                // 1) Risk-driven force close takes priority over the funding decision.
                if (position != null && position.isOpen()) {
                    CarryRiskGuard.Verdict forceClose = riskGuard.shouldForceClose(position, snapshot, dailyPnl, props);
                    if (forceClose.isFlag()) {
                        log.warn("FundingCarryScheduler force-close {}: {}", key, forceClose.getReason());
                        if (executor.close(position)) {
                            positions.remove(key);
                        }
                        if (props.isKillSwitchEnabled() && forceClose.getReason().contains("kill switch")) {
                            halted = true;
                            log.error("FundingCarryScheduler kill switch tripped — halting new activity");
                        }
                        continue;
                    }
                }

                // 2) Funding decision.
                CarryDecision decision = engine.decide(snapshot, position, props);
                switch (decision.getAction()) {
                    case OPEN:
                        handleOpen(key, snapshot, decision, dailyPnl);
                        break;
                    case CLOSE:
                        if (position != null && executor.close(position)) {
                            positions.remove(key);
                        }
                        log.info("FundingCarryScheduler closed {}: {}", key, decision.getReason());
                        break;
                    case HOLD:
                        log.debug("FundingCarryScheduler hold {}: {}", key, decision.getReason());
                        break;
                    case SKIP:
                    default:
                        log.debug("FundingCarryScheduler skip {}: {}", key, decision.getReason());
                        break;
                }
            }
        }
    }

    private void handleOpen(String key, FundingSnapshot snapshot, CarryDecision decision, BigDecimal dailyPnl) {
        CarryRiskGuard.Verdict verdict = riskGuard.canOpen(
                currentTotalNotional(), props.getNotionalPerPairUsdt(), dailyPnl, true, props);
        if (!verdict.isFlag()) {
            log.info("FundingCarryScheduler open blocked for {}: {}", key, verdict.getReason());
            return;
        }
        CarryTradeGateway.OpenResult result = executor.open(snapshot, props.getNotionalPerPairUsdt());
        if (result.isSuccess()) {
            positions.put(key, result.getPosition());
            log.info("FundingCarryScheduler opened {} (annualized={}, basis={})",
                    key, decision.getAnnualizedFunding(), decision.getBasis());
        } else {
            log.error("FundingCarryScheduler open failed for {}: {}", key, result.getError());
        }
    }

    private BigDecimal currentTotalNotional() {
        return positions.values().stream()
                .filter(CarryPosition::isOpen)
                .map(CarryPosition::getNotionalUsdt)
                .filter(n -> n != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Placeholder daily PnL: sums collected funding across open positions. TODO: account for fees, basis
     * pnl and realized results once the live gateway reports fills.
     */
    private BigDecimal computeDailyPnl() {
        return positions.values().stream()
                .map(p -> p.getAccumulatedFundingUsdt() == null ? BigDecimal.ZERO : p.getAccumulatedFundingUsdt())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
