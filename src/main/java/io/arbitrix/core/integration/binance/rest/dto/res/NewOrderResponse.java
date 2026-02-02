package io.arbitrix.core.integration.binance.rest.dto.res;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import io.arbitrix.core.common.enums.OrderStatus;
import io.arbitrix.core.common.enums.TimeInForce;
import io.arbitrix.core.integration.binance.constant.BinanceApiConstants;
import io.arbitrix.core.common.enums.OrderSide;
import io.arbitrix.core.common.enums.OrderType;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Response returned when placing a new order on the system.
 *
 * @author jonathan.ji
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class NewOrderResponse {

    /**
     * Order symbol.
     */
    private String symbol;

    /**
     * Order id.
     */
    private Long orderId;

    /**
     * This will be either a generated one, or the newClientOrderId parameter
     * which was passed when creating the new order.
     */
    private String clientOrderId;

    private String price;

    private String origQty;

    private String executedQty;

    private String cummulativeQuoteQty;

    private OrderStatus status;

    private TimeInForce timeInForce;

    private OrderType type;

    private OrderSide side;

    private List<TradeFill> fills;

    /**
     * Transact time for this order.
     */
    private Long transactTime;

    @Override
    public String toString() {
        return new ToStringBuilder(this, BinanceApiConstants.TO_STRING_BUILDER_STYLE)
                .append("symbol", symbol)
                .append("orderId", orderId)
                .append("clientOrderId", clientOrderId)
                .append("transactTime", transactTime)
                .append("price", price)
                .append("origQty", origQty)
                .append("executedQty", executedQty)
                .append("status", status)
                .append("timeInForce", timeInForce)
                .append("type", type)
                .append("side", side)
                .append("fills", Optional.ofNullable(fills).orElse(Collections.emptyList())
                        .stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(", ")))
                .toString();
    }
}
