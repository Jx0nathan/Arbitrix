package io.arbitrix.core.integration.binance.streamer;

import com.binance.connector.client.impl.WebSocketStreamClientImpl;
import io.arbitrix.core.common.util.JacksonUtil;
import io.arbitrix.core.common.util.TrackingUtils;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import io.arbitrix.core.common.enums.ExchangeNameEnum;
import io.arbitrix.core.common.event.BookTickerEvent;
import io.arbitrix.core.common.monitor.WSSMonitor;
import io.arbitrix.core.integration.binance.config.BinanceProperties;
import io.arbitrix.core.strategy.base.action.BookTickerEventListener;
import io.arbitrix.core.strategy.base.condition.ExchangeConditional;
import io.arbitrix.core.strategy.base.condition.ExecuteStrategyConditional;
import io.arbitrix.core.utils.ExchangeMarketOpenUtilV2;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * 按Symbol的最优挂单信息流，分配给感兴趣的监听器
 *
 * @author jonathan.ji
 */
@Log4j2
@Component
@ExchangeConditional(exchangeName = "BINANCE")
@ExecuteStrategyConditional(executeStrategyName = "pure_market_making")
@AllArgsConstructor
public class BinanceBookTickerStreamer {
    private final List<BookTickerEventListener> quotesListenerList;
    private final BinanceProperties binanceMbxProperties;
    private final ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil;

    @PostConstruct
    public void startBookTickerStreaming() {
        List<String> symbolList = exchangeMarketOpenUtil.getSymbolPairs(ExchangeNameEnum.BINANCE);
        if (!CollectionUtils.isEmpty(symbolList)) {
            WebSocketStreamClientImpl client = new WebSocketStreamClientImpl(binanceMbxProperties.getWsBaseUrl());
            for (String symbol : symbolList) {
                client.bookTicker(symbol.trim(), response -> {
                    WSSMonitor.receiveBookTickerEvent(ExchangeNameEnum.BINANCE.name());
                    try {
                        BookTickerEvent message = JacksonUtil.toObj(response, BookTickerEvent.class);
                        quotesListenerList.forEach(quotesListener -> {
                            quotesListener.onBookTicker(ExchangeNameEnum.BINANCE.name(), message);
                        });
                    } finally {
                        TrackingUtils.clearTrace();
                    }
                });
            }
        } else {
            log.warn("BinanceBookTickerStreamer.startBookTickerStreaming.symbolList is empty");
        }
    }
}
