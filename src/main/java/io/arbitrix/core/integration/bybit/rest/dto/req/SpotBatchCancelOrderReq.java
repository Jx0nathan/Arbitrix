package io.arbitrix.core.integration.bybit.rest.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.arbitrix.core.common.domain.Order;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpotBatchCancelOrderReq implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * Product type
     */
    private String category;
    /**
     * request
     */
    private List<SpotCancelOrderReq> request;

    public static SpotBatchCancelOrderReq convertFromOrders(String category, List<Order> orders) {
        SpotBatchCancelOrderReq req = new SpotBatchCancelOrderReq();
        req.setCategory(category);
        req.setRequest(SpotCancelOrderReq.convertFromOrders(orders));
        return req;
    }
}
