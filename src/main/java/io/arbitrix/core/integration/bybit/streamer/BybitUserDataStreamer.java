package io.arbitrix.core.integration.bybit.streamer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import io.arbitrix.core.common.enums.ExchangeNameEnum;
import io.arbitrix.core.common.enums.ExecutionType;
import io.arbitrix.core.common.enums.OrderStatus;
import io.arbitrix.core.common.event.OrderTradeUpdateEvent;
import io.arbitrix.core.common.monitor.WSSMonitor;
import io.arbitrix.core.integration.bybit.wss.BybitWebSocketClient;
import io.arbitrix.core.integration.bybit.wss.dto.res.ExecutionRes;
import io.arbitrix.core.integration.bybit.wss.dto.res.WSStreamBaseRes;
import io.arbitrix.core.strategy.base.action.OrderTradeUpdateListener;
import io.arbitrix.core.strategy.base.condition.ExchangeConditional;
import io.arbitrix.core.utils.ExchangeMarketOpenUtilV2;
import io.arbitrix.core.common.util.JacksonUtil;

import javax.annotation.PostConstruct;
import java.util.List;

@Log4j2
@Component
@ExchangeConditional(exchangeName = "BYBIT")
@ConditionalOnBean(OrderTradeUpdateListener.class)
public class BybitUserDataStreamer {
    private static final String EXECUTION_SPOT_TOPIC = "execution.spot";
    private final List<OrderTradeUpdateListener> orderTradeUpdateListenerList;
    private final ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil;
    private final BybitWebSocketClient bybitWebSocketClient;

    public BybitUserDataStreamer(List<OrderTradeUpdateListener> orderTradeUpdateListenerList, ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil, BybitWebSocketClient bybitWebSocketClient) {
        this.orderTradeUpdateListenerList = orderTradeUpdateListenerList;
        this.exchangeMarketOpenUtil = exchangeMarketOpenUtil;
        this.bybitWebSocketClient = bybitWebSocketClient;
    }

    @PostConstruct
    private void startUserDataUpdateStreaming() {
        List<String> symbolList = exchangeMarketOpenUtil.getSymbolPairs(ExchangeNameEnum.BYBIT);
        if (CollectionUtils.isEmpty(symbolList)) {
            log.warn("BybitUserDataStreamer.startUserDataUpdateStreaming.symbolList is empty");
            return;
        }
        // TODO 2023/11/3 future需要调整topic
        List<String> subscribeReqs = Lists.newArrayList(EXECUTION_SPOT_TOPIC);
        bybitWebSocketClient.subscribePrivate(subscribeReqs, response -> {
            WSSMonitor.receiveOrderTradeEvent(ExchangeNameEnum.BYBIT.name());
            WSStreamBaseRes<List<ExecutionRes>> orderInfoListRes = JacksonUtil.from(response, new TypeReference<>() {
            });
            List<OrderTradeUpdateEvent> orderTradeUpdateEventList = ExecutionRes.convert2OrderTradeUpdateEvents(orderInfoListRes.getData());
            if (CollectionUtils.isEmpty(orderTradeUpdateEventList)) {
                log.debug("BybitBookTickerStreamer.startBookTickerStreaming.bookTickerEvents is empty and ignore");
                return;
            }
            for (OrderTradeUpdateEvent orderTradeUpdateEvent : orderTradeUpdateEventList) {
                // TODO 2023/10/18 目前order status的判断只在此交易所中,后面如果测试没问题的话就在其他交易所也加上
                if (orderTradeUpdateEvent.getExecutionType() == ExecutionType.TRADE && OrderStatus.FILLED.equals(orderTradeUpdateEvent.getOrderStatus())) {
                    orderTradeUpdateListenerList.forEach(orderTradeUpdateListener -> {
                        orderTradeUpdateListener.orderTradeUpdateEvent(ExchangeNameEnum.BYBIT.name(), orderTradeUpdateEvent);
                    });
                }
            }
        });
        log.info("BybitUserDataStreamer.startUserDataUpdateStreaming.subscribeReqs:{}", JacksonUtil.toJsonStr(subscribeReqs));
    }
}
