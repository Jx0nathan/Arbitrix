package io.arbitrix.core.facade.market;

import io.arbitrix.core.common.domain.SymbolLimitInfo;
import io.arbitrix.core.common.event.BookTickerEvent;

public interface MarketAction {

    /**
     * get symbol limit info
     * @return
     */
    SymbolLimitInfo getSymbolLimitInfo(String symbol, String category);

    /**
     * get server time
     * @return
     */
    String getServerTime();

    /**
     * get last ticker
     * @param symbol
     * @return
     */
    BookTickerEvent lastTicker(String symbol);
    default String symbolLimitInfoCacheKey(String symbol, String category) {
        return category + "-" + symbol;
    }
}
