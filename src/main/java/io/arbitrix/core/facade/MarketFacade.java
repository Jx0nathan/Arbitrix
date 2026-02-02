package io.arbitrix.core.facade;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import io.arbitrix.core.common.domain.SymbolLimitInfo;
import io.arbitrix.core.common.event.BookTickerEvent;
import io.arbitrix.core.facade.market.MarketAction;

import java.util.Map;

@Component
public class MarketFacade implements ExchangeFacade {
    private final Map<String, MarketAction> marketActionMap;

    public MarketFacade(Map<String, MarketAction> marketActionMap) {
        this.marketActionMap = marketActionMap;
    }

    @Override
    public String getPrefix() {
        return "market";
    }

    public SymbolLimitInfo getSymbolLimitInfo(String exchangeName, String category, String symbol) {
        return marketActionMap.get(getActionName(exchangeName)).getSymbolLimitInfo(symbol,category);
    }
    public long getServerTime(String exchangeName) {
        String serverTime = marketActionMap.get(getActionName(exchangeName)).getServerTime();
        if (StringUtils.isEmpty(serverTime)) {
            return -1L;
        }
        return Long.parseLong(serverTime);
    }

    public BookTickerEvent lastTicker(String exchangeName, String symbol) {
        return marketActionMap.get(getActionName(exchangeName)).lastTicker(symbol);
    }
}
