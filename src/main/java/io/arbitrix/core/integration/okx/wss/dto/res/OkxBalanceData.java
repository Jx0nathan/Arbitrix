package io.arbitrix.core.integration.okx.wss.dto.res;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import io.arbitrix.core.common.response.AccountBalance;

import java.util.List;

@NoArgsConstructor
@Data
public class OkxBalanceData {

    /**
     * Adjusted / Effective equity in USD
     * The net fiat value of the assets in the account that can provide margins for spot, futures, perpetual swap and options under the cross margin mode.
     * Cause in multi-ccy or PM mode, the asset and margin requirement will all be converted to USD value to process the order check or liquidation.
     * Due to the volatility of each currency market, our platform calculates the actual USD value of each currency based on discount rates to balance market risks.
     * Applicable to Multi-currency margin and Portfolio margin
     */
    private String adjEq;
    /**
     * Potential borrowing IMR of the account in USD
     * Only applicable to Multi-currency margin and Portfolio margin. It is "" for other margin modes.
     */
    private String borrowFroz;
    /**
     * Initial margin requirement in USD
     * The sum of initial margins of all open positions and pending orders under cross margin mode in USD.
     * Applicable to Multi-currency margin and Portfolio margin
     */
    private String imr;
    /**
     * Isolated margin equity of the currency
     * Applicable to Single-currency margin and Multi-currency margin and Portfolio margin
     */
    private String isoEq;
    /**
     * Margin ratio in USD
     * The index for measuring the risk of a certain asset in the account.
     * Applicable to Multi-currency margin and Portfolio margin
     */
    private String mgnRatio;
    /**
     * Maintenance margin requirement in USD
     * The sum of maintenance margins of all open positions under cross margin mode in USD.
     * Applicable to Multi-currency margin and Portfolio margin
     */
    private String mmr;
    /**
     * Notional value of positions in USD
     * Applicable to Multi-currency margin and Portfolio margin
     */
    private String notionalUsd;
    /**
     * Margin frozen for pending cross orders in USD
     * Only applicable to Multi-currency margin
     */
    private String ordFroz;
    /**
     * The total amount of equity in USD
     */
    private String totalEq;
    /**
     * The latest time to get account information, millisecond format of Unix timestamp, e.g. 1597026383085
     */
    private String uTime;
    private List<OkxBalanceDataDetail> details;

    public AccountBalance convert2AccountBalance(int priority) {
        AccountBalance accountBalance = new AccountBalance();
        accountBalance.setAccountType(String.valueOf(priority));
        if (StringUtils.isEmpty(totalEq)) {
            accountBalance.setTotalWalletBalance("0");
        }else {
            accountBalance.setTotalWalletBalance(totalEq);
        }
        if (StringUtils.isEmpty(adjEq)) {
            accountBalance.setTotalAvailableBalance("0");
        }else {
            accountBalance.setTotalAvailableBalance(adjEq);
        }
        if (StringUtils.isEmpty(totalEq)) {
            accountBalance.setTotalEquity("0");
        }else {
            accountBalance.setTotalEquity(totalEq);
        }
        return accountBalance;
    }
}
