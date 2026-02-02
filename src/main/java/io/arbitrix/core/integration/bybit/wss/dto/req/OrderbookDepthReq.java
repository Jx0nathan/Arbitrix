package io.arbitrix.core.integration.bybit.wss.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderbookDepthReq {
    private static final String ORDER_BOOK_DEPTH_FORMAT = "orderbook.%s.%s";

    private String depth;

    private String symbol;

    public String getTopic() {
        return String.format(ORDER_BOOK_DEPTH_FORMAT, depth, symbol);
    }

    public static OrderbookDepthReq oneDepth(String symbol) {
        return new OrderbookDepthReq("1", symbol);
    }
}