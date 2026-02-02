package io.arbitrix.core.integration.bybit.rest.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpotOpenOrderReq implements Serializable {

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
     * max number of results, default 50, max 50
     */
    private Integer limit = 50;
}
