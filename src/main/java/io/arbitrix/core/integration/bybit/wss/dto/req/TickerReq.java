package io.arbitrix.core.integration.bybit.wss.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TickerReq {
    private static final String TICKER_FORMAT = "tickers.%s";

    private String symbol;

    public String getTopic() {
        return String.format(TICKER_FORMAT, symbol);
    }

    public static TickerReq withSymbol(String symbol) {
        return new TickerReq(symbol);
    }
}