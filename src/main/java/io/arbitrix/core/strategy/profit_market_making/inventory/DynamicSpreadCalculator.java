package io.arbitrix.core.strategy.profit_market_making.inventory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import io.arbitrix.core.strategy.base.condition.ExecuteStrategyConditional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayDeque;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态价差计算器：根据最近价格波动率，实时调整做市价差倍数。
 *
 * <p>逻辑：
 * <ul>
 *   <li>使用滑动窗口维护最近 N 次价格，计算收益率标准差（即波动率）</li>
 *   <li>当波动率高于基准时，价差倍数增大（最大 {@link #MAX_VOLATILITY_MULTIPLIER}）</li>
 *   <li>当波动率低于基准时，价差维持基础值（倍数=1）</li>
 * </ul>
 *
 * @author 3-X
 */
@Slf4j
@Component
@ExecuteStrategyConditional(executeStrategyName = "profit_market_making")
public class DynamicSpreadCalculator {

    /**
     * 波动率计算窗口大小
     */
    private static final int VOLATILITY_WINDOW = 100;

    /**
     * 基准波动率：低于此值不放宽价差（约 0.05%/tick）
     */
    private static final double BASELINE_VOLATILITY = 0.0005;

    /**
     * 波动率最大价差倍数（波动率极高时最多放宽到 2 倍）
     */
    private static final BigDecimal MAX_VOLATILITY_MULTIPLIER = new BigDecimal("2.0");

    // symbol -> 价格滑动窗口（使用 Welford 在线算法）
    private final ConcurrentHashMap<String, VolatilityState> stateMap = new ConcurrentHashMap<>();

    /**
     * 每次收到新价格时更新波动率状态
     *
     * @param symbol   交易对
     * @param midPrice 当前中间价
     */
    public void updatePrice(String symbol, BigDecimal midPrice) {
        stateMap.compute(symbol, (k, state) -> {
            if (state == null) state = new VolatilityState();
            state.addPrice(midPrice.doubleValue());
            return state;
        });
    }

    /**
     * 获取当前波动率调整倍数。
     *
     * @param symbol 交易对
     * @return 价差倍数（1.0 ~ MAX_VOLATILITY_MULTIPLIER）
     */
    public BigDecimal getVolatilityMultiplier(String symbol) {
        VolatilityState state = stateMap.get(symbol);
        if (state == null || state.count < 10) {
            return BigDecimal.ONE;  // 数据不足，不调整
        }

        double volatility = state.getVolatility();
        if (volatility <= BASELINE_VOLATILITY) {
            return BigDecimal.ONE;
        }

        // 将波动率线性映射到 [1.0, MAX]
        // multiplier = 1 + (volatility / BASELINE - 1) * (MAX - 1)，上限 MAX
        double ratio = Math.min(volatility / BASELINE_VOLATILITY, 3.0);  // 超过3倍基准波动率就封顶
        double multiplier = 1.0 + (ratio - 1.0) / 2.0 * (MAX_VOLATILITY_MULTIPLIER.doubleValue() - 1.0);
        multiplier = Math.min(multiplier, MAX_VOLATILITY_MULTIPLIER.doubleValue());

        BigDecimal result = BigDecimal.valueOf(multiplier).setScale(4, RoundingMode.HALF_UP);
        log.debug("DynamicSpreadCalculator.getVolatilityMultiplier: symbol={}, volatility={}, multiplier={}",
                symbol, volatility, result);
        return result;
    }

    /**
     * 使用 Welford 在线算法 + 固定大小滑动窗口维护价格收益率统计量。
     */
    private static class VolatilityState {
        private final ArrayDeque<Double> returns = new ArrayDeque<>(VOLATILITY_WINDOW);
        private double lastPrice = 0;
        private int count = 0;

        // Welford 统计量（针对收益率序列）
        private double runningMean = 0;
        private double runningM2 = 0;
        private int welfordCount = 0;

        void addPrice(double price) {
            if (lastPrice == 0) {
                lastPrice = price;
                return;
            }

            // 计算对数收益率
            double ret = Math.log(price / lastPrice);
            lastPrice = price;
            count++;

            // 滑动窗口：移除最旧的收益率
            if (returns.size() >= VOLATILITY_WINDOW) {
                double oldest = returns.pollFirst();
                removeFromWelford(oldest);
            }

            returns.addLast(ret);
            addToWelford(ret);
        }

        double getVolatility() {
            if (welfordCount < 2) return 0;
            return Math.sqrt(runningM2 / (welfordCount - 1));
        }

        private void addToWelford(double value) {
            welfordCount++;
            double delta = value - runningMean;
            runningMean += delta / welfordCount;
            runningM2 += delta * (value - runningMean);
        }

        private void removeFromWelford(double value) {
            if (welfordCount <= 1) {
                welfordCount = 0;
                runningMean = 0;
                runningM2 = 0;
                return;
            }
            double oldMean = runningMean;
            runningMean = (runningMean * welfordCount - value) / (welfordCount - 1);
            runningM2 -= (value - oldMean) * (value - runningMean);
            runningM2 = Math.max(runningM2, 0.0);
            welfordCount--;
        }
    }
}
