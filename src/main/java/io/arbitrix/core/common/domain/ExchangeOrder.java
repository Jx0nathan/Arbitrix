package io.arbitrix.core.common.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import io.arbitrix.core.common.enums.OrderSide;
import io.arbitrix.core.common.enums.OrderType;
import io.arbitrix.core.integration.binance.constant.BinanceApiConstants;
import io.arbitrix.core.common.enums.NewOrderResponseType;
import io.arbitrix.core.common.enums.TimeInForce;
import io.arbitrix.core.utils.SystemClock;

/**
 * A trade order to enter or exit a position.
 *
 * @author jonathan.ji
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ExchangeOrder {

    /**
     * Symbol to place the order on.
     */
    private String symbol;

    /**
     * Buy/Sell order side.
     */
    private OrderSide side;

    /**
     * Type of order.
     */
    private OrderType type;

    /**
     * Time in force to indicate how long will the order remain active.
     */
    private TimeInForce timeInForce;

    /**
     * Quantity.
     */
    private String quantity;

    /**
     * Quote quantity.
     */
    private String quoteOrderQty;

    /**
     * Price.
     */
    private String price;

    /**
     * A unique id for the order. Automatically generated if not sent.
     */
    private String newClientOrderId;

    /**
     * Used with stop orders.
     */
    private String stopPrice;

    /**
     * Used with stop orders.
     */
    private String stopLimitPrice;

    /**
     * Used with iceberg orders.
     */
    private String icebergQty;

    /**
     * Set the response JSON. ACK, RESULT, or FULL; default: RESULT.
     */
    private NewOrderResponseType newOrderRespType;

    /**
     * Receiving window.
     */
    private Long recvWindow;

    /**
     * Order timestamp.
     */
    private long timestamp;

    private Long eventTime;

    /**
     * 仓位减仓, reduceOnly=true
     */
    private String reduceOnly;

    /**
     * 订单价格的层级
     */
    private int priceLevel;

    /**
     * Creates a new order with all required parameters.
     */
    public ExchangeOrder(String symbol, OrderSide side, OrderType type, TimeInForce timeInForce, String quantity) {
        this.symbol = symbol;
        this.side = side;
        this.type = type;
        this.timeInForce = timeInForce;
        this.quantity = quantity;
        this.newOrderRespType = NewOrderResponseType.RESULT;
        this.timestamp = System.currentTimeMillis();
        this.recvWindow = BinanceApiConstants.DEFAULT_RECEIVING_WINDOW;
        this.eventTime = SystemClock.now();
    }

    /**
     * Creates a new order with all required parameters plus price, which is optional for MARKET orders.
     */
    public ExchangeOrder(String symbol, OrderSide side, OrderType type, TimeInForce timeInForce, String quantity, String price, String newClientOrderId) {
        this(symbol, side, type, timeInForce, quantity);
        this.price = price;
        this.newClientOrderId = newClientOrderId;
        this.eventTime = SystemClock.now();
    }


    /**
     * Places a LIMIT_MAKER buy order for the given <code>quantity</code>.
     *
     * @return a new order which is pre-configured with LIMIT_MAKER as the order type and BUY as the order side.
     */
    public static ExchangeOrder limitMarketBuy(String symbol, String quantity, String price, String newClientOrderId) {
        return new ExchangeOrder(symbol, OrderSide.BUY, OrderType.LIMIT_MAKER, null, quantity, price, newClientOrderId);
    }

    /**
     * Places a LIMIT_MAKER sell order for the given <code>quantity</code>.
     *
     * @return a new order which is pre-configured with LIMIT_MAKER as the order type and SELL as the order side.
     */
    public static ExchangeOrder limitMarketSell(String symbol, String quantity, String price, String newClientOrderId) {
        return new ExchangeOrder(symbol, OrderSide.SELL, OrderType.LIMIT_MAKER, null, quantity, price, newClientOrderId);
    }

    /**
     * Places a LIMIT buy order for the given <code>quantity</code> and <code>price</code>.
     *
     * @return a new order which is pre-configured with LIMIT as the order type and BUY as the order side.
     */
    public static ExchangeOrder limitBuy(String symbol, TimeInForce timeInForce, String quantity, String price, String newClientOrderId) {
        return new ExchangeOrder(symbol, OrderSide.BUY, OrderType.LIMIT, timeInForce, quantity, price, newClientOrderId);
    }

    /**
     * Places a LIMIT sell order for the given <code>quantity</code> and <code>price</code>.
     *
     * @return a new order which is pre-configured with LIMIT as the order type and SELL as the order side.
     */
    public static ExchangeOrder limitSell(String symbol, TimeInForce timeInForce, String quantity, String price, String newClientOrderId) {
        return new ExchangeOrder(symbol, OrderSide.SELL, OrderType.LIMIT, timeInForce, quantity, price, newClientOrderId);
    }
}
