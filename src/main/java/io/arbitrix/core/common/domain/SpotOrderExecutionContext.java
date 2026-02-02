package io.arbitrix.core.common.domain;

import lombok.*;
import lombok.extern.log4j.Log4j2;
import io.arbitrix.core.common.enums.OrderSide;
import io.arbitrix.core.common.event.BookTickerEvent;
import io.arbitrix.core.common.event.OrderTradeUpdateEvent;
import io.arbitrix.core.common.orderbook.OwnOrderBook;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author jonathan.ji
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Log4j2
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SpotOrderExecutionContext extends OrderExecutionContext {

    private int orderLevel;

    private String exchangeName;

    private String symbol;

    private OrderSide orderSide;

    private BookTickerEvent bookTickerEvent;

    private Map<String, OrderTradeUpdateEvent> orderTradeUpdateEventMap;

    private Map<String, OwnOrderBook> ownOrderBook;

    /**
     * 获取最优挂单买价
     */
    public BigDecimal getBidPrice() {
        if (bookTickerEvent == null) {
            log.warn("bookTickerEvent is null exchange {}, symbol {}, side {}", exchangeName, symbol, orderSide);
            return null;
        }
        return new BigDecimal(bookTickerEvent.getBidPrice());
    }

    /**
     * 获取最优挂单卖价
     */
    public BigDecimal getAskPrice() {
        if (bookTickerEvent == null) {
            log.warn("bookTickerEvent is null exchange {}, symbol {}, side {}", exchangeName, symbol, orderSide);
            return null;
        }
        return new BigDecimal(bookTickerEvent.getAskPrice());
    }

    public boolean isBuy() {
        return orderSide == OrderSide.BUY;
    }

    public boolean isSell() {
        return orderSide == OrderSide.SELL;
    }

}
