package io.arbitrix.core.strategy.base.action;

import io.arbitrix.core.common.util.TrackingUtils;
import io.arbitrix.core.common.event.BookTickerEvent;

import java.util.List;

/**
 * @author jonathan.ji
 */
@FunctionalInterface
public interface BookTickerEventListener {

    /**
     * 订阅Symbol的最优挂单信息流
     *
     * @param exchangeName    交易所的代号
     * @param bookTickerEvent 最优挂单信息流
     */
    void onBookTicker(String exchangeName, BookTickerEvent bookTickerEvent);

    /**
     * 生成traceId
     *
     * @param exchangeName 交易所名称
     * @param symbol       交易对
     * @param bidPrice     买价
     * @param askPrice     卖价
     * @return traceId
     */
    default String generateTraceId(String exchangeName, String symbol, String bidPrice, String askPrice) {
        return String.format("%s_%s_%s_%s_%s", exchangeName, symbol, bidPrice, askPrice, TrackingUtils.generateUUID());
    }
}
