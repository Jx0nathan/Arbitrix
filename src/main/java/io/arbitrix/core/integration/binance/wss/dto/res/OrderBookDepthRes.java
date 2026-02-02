package io.arbitrix.core.integration.binance.wss.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import io.arbitrix.core.common.enums.OrderLevel;
import io.arbitrix.core.common.event.BookTickerEvent;
import io.arbitrix.core.integration.binance.wss.exception.DepthConvert2BookTickerException;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderBookDepthRes {
    private String lastUpdateId;
    private List<List<String>> bids;
    private List<List<String>> asks;

    public BookTickerEvent convertOneDepth2BookTickerEvent(String symbol) {
        if (CollectionUtils.isEmpty(bids) || CollectionUtils.isEmpty(asks)) {
            return null;
        }
        if (bids.get(0).size() != 2 || asks.get(0).size() != 2) {
            throw new DepthConvert2BookTickerException("bids or asks size is not 2");
        }
        BookTickerEvent bookTickerEvent = new BookTickerEvent();
        bookTickerEvent.setSymbol(symbol);
        List<String> bestBid = bids.get(0);
        bookTickerEvent.setBidPrice(bestBid.get(0));
        bookTickerEvent.setBidQuantity(bestBid.get(1));
        List<String> bestAsk = asks.get(0);
        bookTickerEvent.setAskPrice(bestAsk.get(0));
        bookTickerEvent.setAskQuantity(bestAsk.get(1));
        bookTickerEvent.setOrderLevel(OrderLevel.FIRST_LEVEL);
        return bookTickerEvent;
    }
}