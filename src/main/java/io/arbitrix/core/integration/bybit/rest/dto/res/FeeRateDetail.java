package io.arbitrix.core.integration.bybit.rest.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.arbitrix.core.common.response.AccountTradeFee;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeeRateDetail {

    /**
     * Symbol name. Keeps "" for Options
     */
    private String symbol;

    /**
     * Base coin. SOL, BTC, ETH
     */
    private String baseCoin;

    /**
     * Taker fee rate
     */
    private String takerFeeRate;

    /**
     * Maker fee rate
     */
    private String makerFeeRate;

    public AccountTradeFee convert2AccountTradeFee() {
        AccountTradeFee accountTradeFee = new AccountTradeFee();
        accountTradeFee.setSymbol(symbol);
        accountTradeFee.setMakerCommission(makerFeeRate);
        accountTradeFee.setTakerCommission(takerFeeRate);
        return accountTradeFee;
    }
}
