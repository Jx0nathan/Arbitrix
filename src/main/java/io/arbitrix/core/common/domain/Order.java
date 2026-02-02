package io.arbitrix.core.common.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.arbitrix.core.common.enums.OrderSide;
import io.arbitrix.core.common.enums.OrderType;
import io.arbitrix.core.common.enums.OrderStatus;
import io.arbitrix.core.common.enums.TimeInForce;

/**
 * Trade order information.
 *
 * @author jonathan.ji
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    /**
     * Symbol that the order was put on.
     */
    private String symbol;

    /**
     * Order id.
     */
    private String orderId;

    /**
     * Client order id.
     */
    private String clientOrderId;

    /**
     * Price.
     */
    private String price;

    /**
     * Original quantity.
     */
    private String origQty;

    /**
     * Order status.
     */
    private OrderStatus status;

    /**
     * Time in force to indicate how long will the order remain active.
     */
    private TimeInForce timeInForce;

    /**
     * Type of order.
     */
    private OrderType type;

    /**
     * Buy/Sell order side.
     */
    private OrderSide side;

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
     * Order timestamp.
     */
    private long time;

    /**
     * Used to calculate the average price
     */
    private String cummulativeQuoteQty;

    /**
     * Update timestamp.
     */
    private long updateTime;

    /**
     * Is working.
     */
    @JsonProperty("isWorking")
    private boolean working;

    /**
     * Original quote order quantity.
     */
    private String origQuoteOrderQty;

}
