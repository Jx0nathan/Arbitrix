package io.arbitrix.core.integration.bybit.rest.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.arbitrix.core.common.domain.Order;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpotCancelOrderReq implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * Product type
     */
    private String category;
    /**
     * symbol
     */
    private String symbol;

    /**
     * Order Id
     */
    private String orderId;

    /**
     * Client order ID
     */
    @JsonProperty("orderLinkId")
    private String clientOrderId;

    public static List<SpotCancelOrderReq> convertFromOrders(List<Order> orders) {
        return orders.stream().map(order -> SpotCancelOrderReq.builder()
                .symbol(order.getSymbol())
                .orderId(order.getOrderId())
                .clientOrderId(order.getClientOrderId())
                .build()).collect(Collectors.toList());
    }
}
