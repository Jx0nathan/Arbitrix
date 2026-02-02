package io.arbitrix.core.integration.bitget.rest.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpotBatchCancelOrderReq implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * Currency pair
     */
    private String symbol;

    /**
     * Order Ids
     */
    private List<String> orderId;

    /**
     * Client order Ids
     */
    private List<String> clientOid;
}
