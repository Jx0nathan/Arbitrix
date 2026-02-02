package io.arbitrix.core.integration.binance.streamer;

import com.binance.connector.client.impl.WebSocketStreamClientImpl;
import io.arbitrix.core.common.util.JacksonUtil;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import io.arbitrix.core.common.enums.ExchangeNameEnum;
import io.arbitrix.core.common.enums.WSStreamType;
import io.arbitrix.core.common.monitor.WSSMonitor;
import io.arbitrix.core.integration.binance.config.BinanceProperties;
import io.arbitrix.core.integration.binance.ping.BinanceListenKeyService;
import io.arbitrix.core.integration.binance.wss.dto.res.UserDataUpdateEvent;
import io.arbitrix.core.strategy.base.action.OrderTradeUpdateListener;
import io.arbitrix.core.strategy.base.condition.ExchangeConditional;
import io.arbitrix.core.utils.ExchangeMarketOpenUtilV2;
import io.arbitrix.core.utils.SystemClock;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @author jonathan.ji
 */
@Component
@ExchangeConditional(exchangeName = "BINANCE")
@ConditionalOnBean(OrderTradeUpdateListener.class)
public class BinanceUserDataStreamer {
    private final List<OrderTradeUpdateListener> orderTradeUpdateListenerList;
    private final BinanceListenKeyService binanceListenKeyService;
    private final BinanceProperties binanceMbxProperties;
    private final ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil;

    public BinanceUserDataStreamer(List<OrderTradeUpdateListener> orderTradeUpdateListenerList,
                                   BinanceListenKeyService binanceListenKeyService,
                                   BinanceProperties binanceMbxProperties, ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil) {
        this.orderTradeUpdateListenerList = orderTradeUpdateListenerList;
        this.binanceListenKeyService = binanceListenKeyService;
        this.binanceMbxProperties = binanceMbxProperties;
        this.exchangeMarketOpenUtil = exchangeMarketOpenUtil;
    }

    @PostConstruct
    private void startUserDataUpdateStreaming() {
        if (exchangeMarketOpenUtil.exchangeCanStart(ExchangeNameEnum.BINANCE)) {
            WebSocketStreamClientImpl client = new WebSocketStreamClientImpl(binanceMbxProperties.getWsBaseUrl());
            String listenKey = binanceListenKeyService.createListenKey();
            Preconditions.checkState(!StringUtils.isEmpty(listenKey), "listenKey is null, please give value.");
            client.listenUserStream(listenKey, response -> {
                long receiveTime = SystemClock.now();
                UserDataUpdateEvent userDataUpdateEvent = JacksonUtil.toObj(response, UserDataUpdateEvent.class);
                if (userDataUpdateEvent.getEventType() == UserDataUpdateEvent.UserDataUpdateEventType.ORDER_TRADE_UPDATE) {
                    WSSMonitor.recordDelay(ExchangeNameEnum.BINANCE.name(), WSStreamType.PRIVATE, userDataUpdateEvent, receiveTime);
                    WSSMonitor.receiveOrderTradeEvent(ExchangeNameEnum.BINANCE.name());
                    // 缓存使用的都是origClientOrderId，所以这里需要把newClientOrderId赋值给origClientOrderId
                    userDataUpdateEvent.getOrderTradeUpdateEvent().setOrigClientOrderId(userDataUpdateEvent.getOrderTradeUpdateEvent().getNewClientOrderId());
                    orderTradeUpdateListenerList.forEach(orderTradeUpdateListener -> {
                        orderTradeUpdateListener.orderTradeUpdateEvent(ExchangeNameEnum.BINANCE.name(), userDataUpdateEvent.getOrderTradeUpdateEvent());
                    });
                }
            });
        }
    }
}
