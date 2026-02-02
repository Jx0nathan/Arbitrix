package io.arbitrix.core.strategy.base.action;

import io.arbitrix.core.common.event.OrderTradeUpdateEvent;

@FunctionalInterface
public interface OrderTradeUpdateListener {

    /**
     * 订阅Symbol的订单成交信息流
     *
     * @param exchangeName          交易所的代号
     * @param orderTradeUpdateEvent 订单成交信息流
     */
    void orderTradeUpdateEvent(String exchangeName, OrderTradeUpdateEvent orderTradeUpdateEvent);

}
