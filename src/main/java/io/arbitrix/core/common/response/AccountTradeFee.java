package io.arbitrix.core.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jonathan.ji
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountTradeFee {

    /**
     * 交易对
     */
    private String symbol;

    /**
     * maker手续费
     */
    private String makerCommission;

    /**
     * taker手续费
     */
    private String takerCommission;

}
