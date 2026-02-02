package io.arbitrix.core.integration.bitget.wss.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import io.arbitrix.core.common.event.BookTickerEvent;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author mcx
 * @date 2023/9/26
 * @description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookTicker {
    private String instId;
    private String last;
    private String open24h;
    private String high24h;
    private String low24h;
    private String bestBid;
    private String bestAsk;
    private String baseVolume;
    private String quoteVolume;
    private Long ts;
    private Integer labeId;
    private String openUtc;
    private String chgUTC;
    private String bidSz;
    private String askSz;

    public static List<BookTickerEvent> convert2BookTickerEventList(List<BookTicker> bookTickerList) {
        if (CollectionUtils.isEmpty(bookTickerList)) {
            return Collections.emptyList();
        }
        return bookTickerList.stream().map(BookTicker::convert2BookTickerEvent).collect(Collectors.toList());
    }

    public static BookTickerEvent convert2BookTickerEvent(BookTicker bookTicker) {
        BookTickerEvent bookTickerEvent = new BookTickerEvent();
        bookTickerEvent.setSymbol(bookTicker.getInstId());
        bookTickerEvent.setBidPrice(bookTicker.getBestBid());
        bookTickerEvent.setBidQuantity(bookTicker.getBidSz());
        bookTickerEvent.setAskPrice(bookTicker.getBestAsk());
        bookTickerEvent.setAskQuantity(bookTicker.getAskSz());
        return bookTickerEvent;
    }
}
