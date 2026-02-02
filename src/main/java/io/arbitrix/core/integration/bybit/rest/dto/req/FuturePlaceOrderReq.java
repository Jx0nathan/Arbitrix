package io.arbitrix.core.integration.bybit.rest.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.arbitrix.core.common.enums.TimeInForce;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FuturePlaceOrderReq implements Serializable {

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
     * Order direction
     */
    private String side;
    /**
     * Order type
     */
    private String orderType;
    /**
     * order quantity
     */
    private String qty;
    /**
     * order price
     */
    private String price;
    /**
     * Time in force, default GTC
     */
    private String timeInForce = TimeInForce.GTC.name();

    /**
     *
     */
    private Integer positionIdx;
    /**
     * Client order ID
     */
    @JsonProperty("orderLinkId")
    private String clientOrderId;
}
