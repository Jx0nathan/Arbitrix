package io.arbitrix.core.integration.okx.rest.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jonathan.ji
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountTradeFeeRequest {

    /**
     * 产品类型
     */
    private String instType;

    /**
     * 产品ID，如 BTC-USDT
     */
    private String instId;

    /**
     * 标的指数
     */
    private String uly;

    /**
     * 交易品种
     */
    private String instFamily;

    public AccountTradeFeeRequest(String instType, String instId) {
        this.instType = instType;
        this.instId = instId;
    }
}
