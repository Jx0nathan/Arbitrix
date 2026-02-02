package io.arbitrix.core.strategy.profit_market_making.data;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import io.arbitrix.core.common.event.BookTickerEvent;
import io.arbitrix.core.strategy.base.action.OrderBookDepthEventListener;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jonathan.ji
 */
@Component
@Order(1)
public class SymbolDataHolder implements OrderBookDepthEventListener {

    private static final String BOOK_TICKER_KEY = "%s_%s";

    private final ConcurrentHashMap<String, BookTickerEvent> bookTickerEventConcurrentHashMap = new ConcurrentHashMap<>();

    @Override
    public void onDepthOrderBook(String exchangeName, List<BookTickerEvent> bookTickerEventList) {
        for (BookTickerEvent bookTickerEvent : bookTickerEventList) {
            if ("USDCUSDT".equalsIgnoreCase(bookTickerEvent.getSymbol()) || "BTCUSDT".equalsIgnoreCase(bookTickerEvent.getSymbol())) {
                pushBookTickerIntoCache(exchangeName, bookTickerEvent.getSymbol(), bookTickerEvent);
            }
        }
    }

    public void pushBookTickerIntoCache(String exchangeName, String symbol, BookTickerEvent bookTickerEvent) {
        String cacheKey = String.format(BOOK_TICKER_KEY, exchangeName.toUpperCase(), symbol.toUpperCase());
        bookTickerEventConcurrentHashMap.put(cacheKey, bookTickerEvent);
    }

    public BookTickerEvent getBookTickerFromCache(String exchangeName, String symbol) {
        String cacheKey = String.format(BOOK_TICKER_KEY, exchangeName.toUpperCase(), symbol.toUpperCase());
        return bookTickerEventConcurrentHashMap.get(cacheKey);
    }
}
