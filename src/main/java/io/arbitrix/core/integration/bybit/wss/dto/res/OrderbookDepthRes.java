package io.arbitrix.core.integration.bybit.wss.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import io.arbitrix.core.common.event.BookTickerEvent;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderbookDepthRes {
    @JsonProperty("s")
    private String symbol;
    @JsonProperty("b")
    private List<List<String>> bids;
    @JsonProperty("a")
    private List<List<String>> asks;
    @JsonProperty("u")
    private Long updateId;
    private Long seq;

    public BookTickerEvent convertOneDepth2BookTickerEvent() {
        BookTickerEvent bookTickerEvent = new BookTickerEvent();
        bookTickerEvent.setSymbol(symbol);
        if (CollectionUtils.isNotEmpty(bids) && bids.get(0).size() == 2) {
            List<String> bestBid = bids.get(0);
            bookTickerEvent.setBidPrice(bestBid.get(0));
            bookTickerEvent.setBidQuantity(bestBid.get(1));
        }
        if (CollectionUtils.isNotEmpty(asks) && asks.get(0).size() == 2) {
            List<String> bestAsk = asks.get(0);
            bookTickerEvent.setAskPrice(bestAsk.get(0));
            bookTickerEvent.setAskQuantity(bestAsk.get(1));
        }
        return bookTickerEvent;
    }
}