package io.arbitrix.core.integration.bybit.rest.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import io.arbitrix.core.common.domain.Order;
import io.arbitrix.core.integration.bybit.util.BybitUtil;

import java.io.Serializable;

@Data
@Log4j2
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpotOpenOrderInfo implements Serializable {

    private static final long serialVersionUID = -1L;
    /**
     * Order ID
     */
    private String orderId;
    /**
     * Client order ID
     */
    @JsonProperty("orderLinkId")
    private String clientOrderId;
    /**
     * symbol
     */
    private String symbol;
    /**
     * Order price
     */
    private String price;
    /**
     * Order quantity
     */
    private String qty;
    /**
     * Order side
     */
    private String side;
    /**
     * Order status
     */
    private String orderStatus;
    /**
     * average price
     */
    private String avgPrice;
    /**
     * Cumulative executed order qty
     */
    private String cumExecQty;
    /**
     * Time in force
     */
    private String timeInForce;
    /**
     * Order type
     */
    private String orderType;
    /**
     * Order creation time
     */
    private String createdTime;
    /**
     * Order update time
     */
    private String updatedTime;

    public Order convert2Orders() {
        try {
            Order order = new Order();
            order.setSymbol(this.getSymbol());
            order.setOrderId(this.getOrderId());
            order.setClientOrderId(this.getClientOrderId());
            order.setPrice(this.getPrice());
            order.setOrigQty(this.getQty());

            order.setStatus(BybitUtil.convertStatus(this.getOrderStatus()));
            order.setSide(BybitUtil.convertSideIn(this.getSide()));
            order.setTime(Long.parseLong(this.getCreatedTime()));
            return order;
        } catch (Exception e) {
            log.error("BybitRestClient.convert2Orders error", e);
        }
        return null;
    }
}
