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
public class SpotCancelOrderReq implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * Currency pair
     */
    private String symbol;

    /**
     * Order Id
     */
    private String orderId;

    /**
     * Client order Id
     */
    private String clientOid;
}
