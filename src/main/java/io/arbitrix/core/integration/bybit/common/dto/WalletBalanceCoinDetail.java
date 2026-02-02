package io.arbitrix.core.integration.bybit.common.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.arbitrix.core.common.response.CoinBalance;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletBalanceCoinDetail {

    private String coin;

    private String equity;

    private String usdValue;

    private String walletBalance;

    private String free;

    private String locked;

    private String borrowAmount;

    private String availableToBorrow;

    private String availableToWithdraw;

    private String accruedInterest;

    private String totalOrderIM;

    private String totalPositionIM;

    private String totalPositionMM;

    private String unrealisedPnl;

    private String cumRealisedPnl;

    private String bonus;

    private String collateralSwitch;

    private String marginCollateral;
    public CoinBalance convert2CoinBalance() {
        CoinBalance coinBalance = new CoinBalance();
        coinBalance.setCoin(coin);
        coinBalance.setAvailable(walletBalance);
        coinBalance.setLocked(locked);
        return coinBalance;
    }

}
