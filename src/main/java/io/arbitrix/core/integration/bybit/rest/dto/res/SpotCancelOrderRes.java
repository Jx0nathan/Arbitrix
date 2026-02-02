package io.arbitrix.core.integration.bybit.rest.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpotCancelOrderRes implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * Client order ID
     */
    @JsonProperty("orderLinkId")
    private String clientOrderId;
    /**
     * Order ID
     */
    private String orderId;
    /**
     * symbol, 只在批量取消的时候返回
     */
    private String symbol;
}
