package io.arbitrix.core.integration.bybit.wss.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import io.arbitrix.core.common.event.BookTickerEvent;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TickerRes {
    private String symbol;
    private String lastPrice;
    private String highPrice24h;
    private String lowPrice24h;
    private String prevPrice24h;
    private String volume24h;
    private String turnover24h;
    private String price24hPcnt;
    private String usdIndexPrice;

    public BookTickerEvent convert2BookTickerEvent() {
        if (StringUtils.isEmpty(lastPrice)) {
            return null;
        }
        BookTickerEvent bookTickerEvent = new BookTickerEvent();
        bookTickerEvent.setSymbol(symbol);
        bookTickerEvent.setLastPrice(lastPrice);
        return bookTickerEvent;
    }
}