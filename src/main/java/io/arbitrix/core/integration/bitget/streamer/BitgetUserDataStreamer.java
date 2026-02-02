package io.arbitrix.core.integration.bitget.streamer;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import io.arbitrix.core.common.enums.ExchangeNameEnum;
import io.arbitrix.core.common.enums.ExecutionType;
import io.arbitrix.core.common.event.OrderTradeUpdateEvent;
import io.arbitrix.core.common.monitor.WSSMonitor;
import io.arbitrix.core.integration.bitget.util.BitgetUtil;
import io.arbitrix.core.integration.bitget.wss.BitgetWebSocketClient;
import io.arbitrix.core.integration.bitget.wss.dto.req.SubscribeReq;
import io.arbitrix.core.integration.bitget.wss.dto.res.OrderInfo;
import io.arbitrix.core.integration.bitget.wss.dto.res.WsBaseRes;
import io.arbitrix.core.strategy.base.action.OrderTradeUpdateListener;
import io.arbitrix.core.utils.ExchangeMarketOpenUtilV2;
import io.arbitrix.core.common.util.JacksonUtil;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Component
@ConditionalOnBean(OrderTradeUpdateListener.class)
public class BitgetUserDataStreamer {
    private final List<OrderTradeUpdateListener> orderTradeUpdateListenerList;
    private final ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil;
    private final BitgetWebSocketClient bitgetWebSocketClient;

    public BitgetUserDataStreamer(List<OrderTradeUpdateListener> orderTradeUpdateListenerList, ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil, BitgetWebSocketClient bitgetWebSocketClient) {
        this.orderTradeUpdateListenerList = orderTradeUpdateListenerList;
        this.exchangeMarketOpenUtil = exchangeMarketOpenUtil;
        this.bitgetWebSocketClient = bitgetWebSocketClient;
    }

    @PostConstruct
    private void startUserDataUpdateStreaming() {
        List<String> symbolList = exchangeMarketOpenUtil.getSymbolPairs(ExchangeNameEnum.BITGET);
        if (CollectionUtils.isEmpty(symbolList)) {
            log.warn("BitgetBookTickerStreamer.startBookTickerStreaming.symbolList is empty");
            return;
        }
        List<SubscribeReq> subscribeReqs = symbolList.stream()
                .map(symbol -> SubscribeReq.orders(BitgetUtil.symbolFromArbitrixToBitgetSpot(symbol)))
                .collect(Collectors.toList());
        bitgetWebSocketClient.subscribe(subscribeReqs, response -> {
            WSSMonitor.receiveOrderTradeEvent(ExchangeNameEnum.BITGET.name());
            WsBaseRes<SubscribeReq, List<OrderInfo>> orderInfoListRes = JacksonUtil.from(response, new TypeReference<>() {
            });
            if (!orderInfoListRes.isSuccess()) {
                log.warn("BitgetBookTickerStreamer.startBookTickerStreaming.error:{}", JacksonUtil.toJsonStr(orderInfoListRes));
                return;
            }
            List<OrderTradeUpdateEvent> orderTradeUpdateEventList = OrderInfo.convert2OrderTradeUpdateEvents(orderInfoListRes.getData());
            if (CollectionUtils.isEmpty(orderTradeUpdateEventList)) {
                log.debug("BitgetBookTickerStreamer.startBookTickerStreaming.bookTickerEvents is empty and ignore");
                return;
            }
            for (OrderTradeUpdateEvent orderTradeUpdateEvent : orderTradeUpdateEventList) {
                if (orderTradeUpdateEvent.getExecutionType() == ExecutionType.TRADE) {
                    orderTradeUpdateListenerList.forEach(orderTradeUpdateListener -> {
                        orderTradeUpdateListener.orderTradeUpdateEvent(ExchangeNameEnum.BITGET.name(), orderTradeUpdateEvent);
                    });
                }
            }
        });
        log.info("BitgetUserDataStreamer.startUserDataUpdateStreaming.subscribeReqs:{}", JacksonUtil.toJsonStr(subscribeReqs));
    }
}
