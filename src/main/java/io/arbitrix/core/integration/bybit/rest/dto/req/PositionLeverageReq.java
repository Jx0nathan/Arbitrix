package io.arbitrix.core.integration.bybit.rest.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jonathan.ji
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PositionLeverageReq {

    /**
     * Product type
     */
    private String category;

    /**
     * Symbol name
     */
    private String symbol;

    /**
     * buyLeverage必須等于sellLeverage
     */
    private String buyLeverage;

    /**
     * buyLeverage必須等于sellLeverage
     */
    private String sellLeverage;

}
