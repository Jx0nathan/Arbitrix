package io.arbitrix.core.integration.bybit.streamer;

import io.arbitrix.core.common.util.TrackingUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import io.arbitrix.core.common.enums.ExchangeNameEnum;
import io.arbitrix.core.common.enums.OrderLevel;
import io.arbitrix.core.common.event.BookTickerEvent;
import io.arbitrix.core.common.monitor.WSSMonitor;
import io.arbitrix.core.integration.bybit.wss.BybitWebSocketClient;
import io.arbitrix.core.integration.bybit.wss.dto.req.OrderbookDepthReq;
import io.arbitrix.core.integration.bybit.wss.dto.res.OrderbookDepthRes;
import io.arbitrix.core.integration.bybit.wss.dto.res.WSStreamBaseRes;
import io.arbitrix.core.strategy.base.action.OrderBookDepthEventListener;
import io.arbitrix.core.strategy.base.condition.ExchangeConditional;
import io.arbitrix.core.strategy.base.condition.ExecuteStrategyConditional;
import io.arbitrix.core.utils.ExchangeMarketOpenUtilV2;
import io.arbitrix.core.common.util.JacksonUtil;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 监听5档数据
 * <p>
 * 配合的策略：PureMarketMakingStrategy
 *
 * @author jonathan.ji
 */
@Log4j2
@Component
@AllArgsConstructor
@ExchangeConditional(exchangeName = "BYBIT")
@ExecuteStrategyConditional(executeStrategyName = "profit_market_making")
public class BybitBookFiveDepthStreamer {
    private final List<OrderBookDepthEventListener> orderBookDepthEventListenerList;
    private final ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil;
    private final BybitWebSocketClient bybitWebSocketClient;
    private final Map<String, BookTickerEvent> bookTickerEventMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void startBookTickerStreaming() {
        List<String> symbolList = exchangeMarketOpenUtil.getSymbolPairs(ExchangeNameEnum.BYBIT);
        if (CollectionUtils.isEmpty(symbolList)) {
            log.warn("BybitBookTickerStreamer.startBookTickerStreaming.symbolList is empty");
            return;
        }
        // 如果是USDC交易对，需要使用USDT的价格来锚定
        symbolList = this.obtainAnchorPairsForUsdc(symbolList);
        List<String> onDepthOrderBook = symbolList.stream()
                .map(symbol -> OrderbookDepthReq.oneDepth(symbol).getTopic())
                .collect(Collectors.toList());
        bybitWebSocketClient.subscribePublic(onDepthOrderBook, response -> {
            try {
                WSSMonitor.receiveDepth1Event(ExchangeNameEnum.BYBIT.name());
                WSStreamBaseRes<OrderbookDepthRes> resMsg = JacksonUtil.from(response, new TypeReference<>() {
                });
                BookTickerEvent bookTickerEvent = convert2BookTickerEvent(resMsg);
                if (Objects.isNull(bookTickerEvent)) {
                    log.warn("BybitBookTickerStreamer.startBookTickerStreaming.onDepthOrderBook.bookTickerEvent is null");
                    return;
                }
                //TODO: 试点功能，暂时只在Bybit上开启，临时使用最优订单的事件流
                orderBookDepthEventListenerList.forEach(orderBookDepthEventListener -> {
                    // 后续监听N档数据的时候，需要设定该事件层级
                    bookTickerEvent.setOrderLevel(OrderLevel.FIRST_LEVEL);
                    orderBookDepthEventListener.onDepthOrderBook(ExchangeNameEnum.BYBIT.name(), List.of(bookTickerEvent));
                });

            } finally {
                TrackingUtils.clearTrace();
            }
        });
        log.info("BybitBookTickerStreamer.startBookTickerStreaming.onDepthOrderBook:{}", JacksonUtil.toJsonStr(onDepthOrderBook));
    }

    private List<String> obtainAnchorPairsForUsdc(List<String> symbolList) {
        if (symbolList.contains("BTCUSDC")) {
            symbolList.add("USDCUSDT");
            symbolList.add("BTCUSDT");
        }
        return symbolList;
    }

    private BookTickerEvent convert2BookTickerEvent(WSStreamBaseRes<OrderbookDepthRes> resMsg) {
        OrderbookDepthRes data = resMsg.getData();
        if (Objects.isNull(data)) {
            log.debug("BybitBookTickerStreamer.convert2BookTickerEvent.data is null");
            return null;
        }
        BookTickerEvent bookTickerEvent = data.convertOneDepth2BookTickerEvent();
        BookTickerEvent oldBookTickerEvent = bookTickerEventMap.get(bookTickerEvent.getSymbol());
        bookTickerEvent.populateAllPriceIfNeed(oldBookTickerEvent);
        bookTickerEventMap.put(bookTickerEvent.getSymbol(), bookTickerEvent);
        return bookTickerEvent;
    }
}
