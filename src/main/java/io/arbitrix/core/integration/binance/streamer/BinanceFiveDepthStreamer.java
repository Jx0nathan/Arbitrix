package io.arbitrix.core.integration.binance.streamer;

import com.binance.connector.client.impl.WebSocketStreamClientImpl;
import io.arbitrix.core.common.util.TrackingUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import io.arbitrix.core.common.enums.ExchangeNameEnum;
import io.arbitrix.core.common.event.BookTickerEvent;
import io.arbitrix.core.common.monitor.WSSMonitor;
import io.arbitrix.core.integration.binance.config.BinanceProperties;
import io.arbitrix.core.integration.binance.wss.DepthWebSocketCallback;
import io.arbitrix.core.integration.binance.wss.dto.res.OrderBookDepthRes;
import io.arbitrix.core.strategy.base.action.OrderBookDepthEventListener;
import io.arbitrix.core.strategy.base.condition.ExchangeConditional;
import io.arbitrix.core.strategy.base.condition.ExecuteStrategyConditional;
import io.arbitrix.core.utils.ExchangeMarketOpenUtilV2;
import io.arbitrix.core.common.util.JacksonUtil;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;

@Log4j2
@Component
@ExchangeConditional(exchangeName = "BINANCE")
@ExecuteStrategyConditional(executeStrategyName = "profit_market_making")
public class BinanceFiveDepthStreamer {
    private final ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil;
    private final List<OrderBookDepthEventListener> orderBookDepthEventListenerList;
    private final BinanceProperties binanceMbxProperties;

    public BinanceFiveDepthStreamer(ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil, List<OrderBookDepthEventListener> orderBookDepthEventListenerList, BinanceProperties binanceMbxProperties) {
        this.exchangeMarketOpenUtil = exchangeMarketOpenUtil;
        this.orderBookDepthEventListenerList = orderBookDepthEventListenerList;
        this.binanceMbxProperties = binanceMbxProperties;
    }

    @PostConstruct
    public void startFiveDepthStreaming() {
        List<String> symbolList = exchangeMarketOpenUtil.getSymbolPairs(ExchangeNameEnum.BINANCE);
        if (CollectionUtils.isEmpty(symbolList)) {
            log.warn("BinanceFiveDepthStreamer.startFiveDepthStreaming.symbolList is empty");
            return;
        }
        // 如果是USDC交易对，需要使用USDT的价格来锚定
        symbolList = this.obtainAnchorPairsForUsdc(symbolList);
        WebSocketStreamClientImpl client = new WebSocketStreamClientImpl(binanceMbxProperties.getWsBaseUrl());
        for (String symbol : symbolList) {
            client.partialDepthStream(symbol.trim(), 5, 100, new DepthWebSocketCallback(symbol, (pushSymbol, response) -> {
                WSSMonitor.receiveDepth5Event(ExchangeNameEnum.BINANCE.name());
                try {
                    OrderBookDepthRes orderbookDepthRes = JacksonUtil.from(response, OrderBookDepthRes.class);
                    //TODO: 试点功能，临时使用最优订单的事件流
                    BookTickerEvent bookTickerEvent = orderbookDepthRes.convertOneDepth2BookTickerEvent(pushSymbol);
                    if (Objects.isNull(bookTickerEvent)) {
                        log.error("BinanceFiveDepthStreamer.startFiveDepthStreaming fail, convert2OneDepthBookTickerEvent return null, symbol is {}, response is {}", pushSymbol, response);
                        return;
                    }
                    orderBookDepthEventListenerList.forEach(orderBookDepthEventListener -> {
                        orderBookDepthEventListener.onDepthOrderBook(ExchangeNameEnum.BINANCE.name(), List.of(bookTickerEvent));
                    });
                } catch (Exception e) {
                    log.error("BinanceFiveDepthStreamer.startFiveDepthStreaming error, symbol is {}, response is {}", pushSymbol, response, e);
                } finally {
                    TrackingUtils.clearTrace();
                }
            }));
        }
    }

    private List<String> obtainAnchorPairsForUsdc(List<String> symbolList) {
        if (symbolList.contains("BTCUSDC")) {
            symbolList.add("USDCUSDT");
            symbolList.add("BTCUSDT");
        }
        return symbolList;
    }
}
