package io.arbitrix.core.integration.bybit.streamer;

import io.arbitrix.core.common.util.TrackingUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import io.arbitrix.core.common.enums.ExchangeNameEnum;
import io.arbitrix.core.common.event.BookTickerEvent;
import io.arbitrix.core.common.monitor.WSSMonitor;
import io.arbitrix.core.integration.bybit.wss.BybitWebSocketClient;
import io.arbitrix.core.integration.bybit.wss.dto.req.OrderbookDepthReq;
import io.arbitrix.core.integration.bybit.wss.dto.res.OrderbookDepthRes;
import io.arbitrix.core.integration.bybit.wss.dto.res.WSStreamBaseRes;
import io.arbitrix.core.strategy.base.action.BookTickerEventListener;
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
 * subscribe book ticker <br>
 *
 * depth 1
 *
 * 配合的策略：PureMarketMakingStrategy
 *
 * @author Charles Meng
 */
@Log4j2
@Component
@AllArgsConstructor
@ExchangeConditional(exchangeName = "BYBIT")
@ExecuteStrategyConditional(executeStrategyName = "pure_market_making")
public class BybitBookTickerStreamer {
    private final List<BookTickerEventListener> quotesListenerList;
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
                quotesListenerList.forEach(quotesListener -> {
                    quotesListener.onBookTicker(ExchangeNameEnum.BYBIT.name(), bookTickerEvent);
                });
            } finally {
                TrackingUtils.clearTrace();
            }
        });
        log.info("BybitBookTickerStreamer.startBookTickerStreaming.onDepthOrderBook:{}", JacksonUtil.toJsonStr(onDepthOrderBook));
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
