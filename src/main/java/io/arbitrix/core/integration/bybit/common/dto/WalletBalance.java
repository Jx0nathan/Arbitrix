package io.arbitrix.core.integration.bybit.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;
import io.arbitrix.core.common.response.AccountBalance;
import io.arbitrix.core.common.response.CoinBalance;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletBalance {

    private String accountType;

    private String accountLTV;

    private String accountIMRate;

    private String accountMMRate;

    private String totalEquity;

    private String totalWalletBalance;

    private String totalMarginBalance;

    private String totalAvailableBalance;

    private String totalPerpUPL;

    private String totalInitialMargin;

    private String totalMaintenanceMargin;

    private List<WalletBalanceCoinDetail> coin;


    public List<CoinBalance> convert2CoinBalances() {
        if (CollectionUtils.isEmpty(coin)) {
            return Collections.emptyList();
        }
        return coin.stream().map(WalletBalanceCoinDetail::convert2CoinBalance).collect(Collectors.toList());

    }

    public AccountBalance convert2CoinBalance() {
        AccountBalance accountBalance = new AccountBalance();
        accountBalance.setAccountType(accountType);
        accountBalance.setTotalWalletBalance(totalWalletBalance);
        accountBalance.setTotalAvailableBalance(totalAvailableBalance);
        accountBalance.setTotalEquity(totalEquity);
        return accountBalance;
    }
}
