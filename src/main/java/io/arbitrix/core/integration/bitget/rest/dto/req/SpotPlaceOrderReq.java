package io.arbitrix.core.integration.bitget.rest.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpotPlaceOrderReq implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * Currency pair
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
     * Order Control Type
     */
    private String force;
    /**
     * Entrusted price, only applicable to price limit order
     */
    private String price;
    /**
     * quantity
     */
    private String quantity;
    /**
     * Client order ID
     */
    private String clientOrderId;
}
