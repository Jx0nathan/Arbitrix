package io.arbitrix.core.strategy.profit_market_making.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import io.arbitrix.core.strategy.base.condition.ExecuteStrategyConditional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 行情趋势过滤器：检测单边趋势行情，暂停做市以避免逆向选择损失。
 *
 * <p>检测逻辑：
 * <ol>
 *   <li>维护最近 {@link #PRICE_WINDOW} 个 tick 的价格队列</li>
 *   <li>比较当前价格与窗口最旧价格的涨跌幅</li>
 *   <li>涨跌幅绝对值超过 {@link #TREND_THRESHOLD} → 判定为趋势行情</li>
 *   <li>触发后冻结 {@link #PAUSE_TICKS} 个 tick，冻结期内不做市</li>
 *   <li>冻结期结束后自动恢复，等待下一次判断</li>
 * </ol>
 *
 * <p>参数说明（1万U做市场景）：
 * <ul>
 *   <li>PRICE_WINDOW = 20：观测最近20个tick，约1~2秒</li>
 *   <li>TREND_THRESHOLD = 0.0015：单方向移动0.15%触发暂停</li>
 *   <li>PAUSE_TICKS = 15：暂停15个tick后恢复观测</li>
 * </ul>
 *
 * @author 3-X
 */
@Slf4j
@Component
@ExecuteStrategyConditional(executeStrategyName = "profit_market_making")
public class TrendFilter {

    /** 价格观测窗口大小（tick 数） */
    private static final int PRICE_WINDOW = 20;

    /** 触发暂停的单向移动阈值（0.15%） */
    private static final BigDecimal TREND_THRESHOLD = new BigDecimal("0.0015");

    /** 触发趋势后暂停的 tick 数 */
    private static final int PAUSE_TICKS = 15;

    private final ConcurrentHashMap<String, SymbolState> stateMap = new ConcurrentHashMap<>();

    /**
     * 更新价格并判断是否处于趋势行情。
     *
     * <p>此方法每个 tick 调用一次，同时完成更新与检测。
     *
     * @param symbol       交易对
     * @param currentPrice 当前 mid price（bid/ask 均可）
     * @return {@code true} = 趋势行情中，暂停做市；{@code false} = 正常，可以挂单
     */
    public boolean isTrending(String symbol, BigDecimal currentPrice) {
        SymbolState state = stateMap.computeIfAbsent(symbol, k -> new SymbolState());
        return state.update(currentPrice);
    }

    /** 查询当前暂停状态（不更新价格） */
    public boolean isPaused(String symbol) {
        SymbolState state = stateMap.get(symbol);
        return state != null && state.pauseCountdown.get() > 0;
    }

    // ── 内部状态 ────────────────────────────────────────────────────────

    private static class SymbolState {
        private final ArrayDeque<BigDecimal> priceWindow = new ArrayDeque<>(PRICE_WINDOW);
        /** 剩余暂停 tick 数，0 表示未暂停 */
        private final AtomicInteger pauseCountdown = new AtomicInteger(0);

        /**
         * 更新价格，返回是否处于暂停状态。
         */
        boolean update(BigDecimal price) {
            // 冻结期倒计时
            int remaining = pauseCountdown.get();
            if (remaining > 0) {
                pauseCountdown.decrementAndGet();
                return true;
            }

            // 维护价格窗口
            if (priceWindow.size() >= PRICE_WINDOW) {
                priceWindow.pollFirst();
            }
            priceWindow.addLast(price);

            // 窗口不足时不做判断
            if (priceWindow.size() < PRICE_WINDOW) {
                return false;
            }

            BigDecimal oldest = priceWindow.peekFirst();
            if (oldest == null || oldest.compareTo(BigDecimal.ZERO) == 0) {
                return false;
            }

            // 计算窗口内涨跌幅
            BigDecimal change = price.subtract(oldest)
                    .divide(oldest, 8, RoundingMode.HALF_UP)
                    .abs();

            if (change.compareTo(TREND_THRESHOLD) >= 0) {
                pauseCountdown.set(PAUSE_TICKS);
                return true;
            }

            return false;
        }
    }
}
