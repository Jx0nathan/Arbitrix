package io.arbitrix.core.integration.bitget.rest.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import io.arbitrix.core.common.domain.Order;
import io.arbitrix.core.integration.bitget.util.BitgetUtil;

import java.io.Serializable;

@Data
@Log4j2
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpotOpenOrderRes implements Serializable {

    private static final long serialVersionUID = -1L;
    /**
     * Account ID
     */
    private String accountId;
    /**
     * symbol
     */
    private String symbol;
    /**
     * Order ID
     */
    private String orderId;
    /**
     * Client order ID
     */
    private String clientOrderId;
    /**
     * Order price
     */
    private String price;
    /**
     * Order quantity
     */
    private String quantity;
    /**
     * Order type
     */
    private String orderType;
    /**
     * Order side
     */
    private String side;
    /**
     * Order status
     */
    private String status;
    /**
     * Filled price
     */
    private String fillPrice;
    /**
     * Filled quantity
     */
    private String fillQuantity;
    /**
     * Filled total amount
     */
    private String fillTotalAmount;
    /**
     * Filled fee
     */
    private String fillFee;
    /**
     * Order source
     */
    private String enterPointSource;
    /**
     * Order creation time
     */
    @JsonProperty("cTime")
    private String createTime;

    public Order convert2Orders() {
        try {
            Order order = new Order();
            order.setSymbol(BitgetUtil.symbolFromBitgetSpotToArbitrix(this.getSymbol().toUpperCase()));
            order.setOrderId(this.getOrderId());
            order.setClientOrderId(this.getClientOrderId());
            order.setPrice(this.getPrice());
            order.setOrigQty(this.getQuantity());
            order.setStatus(BitgetUtil.convertStatus(this.getStatus()));
            order.setSide(BitgetUtil.convertSide(this.getSide()));
            order.setTime(Long.parseLong(this.getCreateTime()));
            return order;
        } catch (Exception e) {
            log.error("BitgetRestClient.convert2Orders error", e);
        }
        return null;
    }
}
