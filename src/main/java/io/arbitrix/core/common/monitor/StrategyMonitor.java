package io.arbitrix.core.common.monitor;

import io.arbitrix.core.common.monitor.utils.MetricsUtils;

import java.time.Duration;

/**
 * @author mcx
 * @date 2023/10/11
 * @description
 */
public class StrategyMonitor {
    public static void recordMarketMakerExecuteOrderThreadGap(Long gapTime, String exchangeName, String symbol, String orderSide) {

        MetricsUtils.recordTimeDefaultPercentiles("market_maker_execute_order_thread_gap_time",
                "market_maker_execute_order_thread_gap_time",
                Duration.ofMillis(gapTime),
                "exchange", exchangeName,
                "symbol", symbol,
                "orderSide", orderSide);
    }
}
