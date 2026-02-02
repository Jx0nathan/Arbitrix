package io.arbitrix.core.common.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;
import io.arbitrix.core.common.enums.TimeInForce;
import io.arbitrix.core.integration.binance.constant.BinanceApiConstants;
import io.arbitrix.core.common.enums.OrderSide;
import io.arbitrix.core.common.enums.OrderType;
import io.arbitrix.core.common.enums.ExecutionType;
import io.arbitrix.core.common.enums.OrderRejectReason;
import io.arbitrix.core.common.enums.OrderStatus;
import io.arbitrix.core.integration.binance.wss.dto.res.UserDataUpdateEvent;

import java.math.BigDecimal;

/**
 * Order or trade report update event.
 * <p>
 * This event is embedded as part of a user data update event.
 *
 * @see UserDataUpdateEvent
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderTradeUpdateEvent {

    /**
     * 事件类型
     */
    @JsonProperty("e")
    private String eventType;

    /**
     * 事件时间
     */
    @JsonProperty("E")
    private Long eventTime;

    /**
     * 交易对
     */
    @JsonProperty("s")
    private String symbol;

    /**
     * clientOrderId
     */
    @JsonProperty("c")
    private String newClientOrderId;

    /**
     * Buy/Sell 订单方向
     */
    @JsonProperty("S")
    private OrderSide side;

    /**
     * 订单类型
     */
    @JsonProperty("o")
    private OrderType type;

    /**
     * 有效方式
     */
    @JsonProperty("f")
    private TimeInForce timeInForce;

    /**
     * 订单原始数量
     */
    @JsonProperty("q")
    private String originalQuantity;

    /**
     * 订单原始价格
     */
    @JsonProperty("p")
    private String price;

    /**
     * 本次事件的具体执行类型
     */
    @JsonProperty("x")
    private ExecutionType executionType;

    /**
     * 订单的当前状态
     */
    @JsonProperty("X")
    private OrderStatus orderStatus;

    /**
     * 订单被拒绝的原因
     */
    @JsonProperty("r")
    private OrderRejectReason orderRejectReason;

    /**
     * orderId
     */
    @JsonProperty("i")
    private Long orderId;

    /**
     * 订单末次成交量
     */
    @JsonProperty("l")
    private String quantityLastFilledTrade;

    /**
     * 订单累计已成交量
     */
    @JsonProperty("z")
    private String accumulatedQuantity;

    /**
     * 订单末次成交价格
     */
    @JsonProperty("L")
    private String priceOfLastFilledTrade;

    /**
     * 手续费数量
     */
    @JsonProperty("n")
    private String commission;

    /**
     * 手续费资产类别
     */
    @JsonProperty("N")
    private String commissionAsset;

    /**
     * 成交时间
     */
    @JsonProperty("T")
    private Long orderTradeTime;

    /**
     * 成交ID
     */
    @JsonProperty("t")
    private Long tradeId;

    /**
     * 订单创建时间
     */
    @JsonProperty("O")
    private Long orderCreationTime;

    /**
     * 订单累计已成交金额
     */
    @JsonProperty("Z")
    private String cumulativeQuoteQty;

    /**
     * 订单末次成交金额
     */
    @JsonProperty("Y")
    private String lastQuoteQty;

    /**
     * Quote Order Qty.
     */
    @JsonProperty("Q")
    private String quoteOrderQty;

    /**
     * note:该字段在收到事件的时候一定要设置,OrderTradeDataManager会根据该字段来判断是否需要清理缓存
     */
    private String origClientOrderId;

    public static OrderTradeUpdateEvent createInitOrderTradeUpdateEvent(OrderSide orderSide) {
        OrderTradeUpdateEvent newOrderTradeBuyEvent = new OrderTradeUpdateEvent();
        newOrderTradeBuyEvent.setPrice(orderSide == OrderSide.BUY ? BigDecimal.ZERO.toString() : BigDecimal.valueOf(Long.MAX_VALUE).toString());
        newOrderTradeBuyEvent.setOriginalQuantity(BigDecimal.ZERO.toString());
        newOrderTradeBuyEvent.setEventTime(System.currentTimeMillis());
        return newOrderTradeBuyEvent;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, BinanceApiConstants.TO_STRING_BUILDER_STYLE)
                .append("eventType", eventType)
                .append("eventTime", eventTime)
                .append("symbol", symbol)
                .append("newClientOrderId", newClientOrderId)
                .append("side", side)
                .append("type", type)
                .append("timeInForce", timeInForce)
                .append("originalQuantity", originalQuantity)
                .append("price", price)
                .append("executionType", executionType)
                .append("orderStatus", orderStatus)
                .append("orderRejectReason", orderRejectReason)
                .append("orderId", orderId)
                .append("quantityLastFilledTrade", quantityLastFilledTrade)
                .append("accumulatedQuantity", accumulatedQuantity)
                .append("priceOfLastFilledTrade", priceOfLastFilledTrade)
                .append("commission", commission)
                .append("commissionAsset", commissionAsset)
                .append("orderTradeTime", orderTradeTime)
                .append("tradeId", tradeId)
                .append("orderCreationTime", orderCreationTime)
                .append("cumulativeQuoteQty", cumulativeQuoteQty)
                .append("lastQuoteQty", lastQuoteQty)
                .append("quoteOrderQty", quoteOrderQty)
                .toString();
    }
}
