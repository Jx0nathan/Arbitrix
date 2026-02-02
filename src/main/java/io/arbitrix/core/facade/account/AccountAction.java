package io.arbitrix.core.facade.account;

import io.arbitrix.core.common.response.AccountTradeFee;
import io.arbitrix.core.common.response.CoinBalance;

import java.util.List;

public interface AccountAction {

    /**
     * 交易手续费率查询
     *
     * @return 交易手续费率的结果集
     */
    List<AccountTradeFee> getAccountTradeFee();

    /**
     * 根据交易对查询交易手续费率
     *
     * @param symbol 特定的交易对
     * @return 交易手续费率的结果集
     */
    AccountTradeFee getAccountTradeFeeBySymbol(String symbol);

    List<CoinBalance> getAllCoinBalance();

    CoinBalance getCoinBalance(String coin);
}
