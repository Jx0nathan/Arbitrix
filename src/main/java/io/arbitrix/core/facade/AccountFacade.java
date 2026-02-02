package io.arbitrix.core.facade;

import org.springframework.stereotype.Component;
import io.arbitrix.core.common.response.AccountTradeFee;
import io.arbitrix.core.common.response.CoinBalance;
import io.arbitrix.core.facade.account.AccountAction;

import java.util.List;
import java.util.Map;

@Component
public class AccountFacade implements ExchangeFacade {
    private final Map<String, AccountAction> accountActionMap;

    public AccountFacade(Map<String, AccountAction> accountActionMap) {
        this.accountActionMap = accountActionMap;
    }

    @Override
    public String getPrefix() {
        return "account";
    }

    public AccountTradeFee getAccountTradeFeeBySymbol(String exchangeName, String symbol) {
        return accountActionMap.get(getActionName(exchangeName)).getAccountTradeFeeBySymbol(symbol);
    }

    public List<AccountTradeFee> getAccountTradeFee(String exchangeName) {
        return accountActionMap.get(getActionName(exchangeName)).getAccountTradeFee();
    }

    public List<CoinBalance> getAllCoinBalance(String exchangeName) {
        return accountActionMap.get(exchangeName).getAllCoinBalance();
    }

    public CoinBalance getCoinBalance(String exchangeName, String coin) {
        return accountActionMap.get(exchangeName).getCoinBalance(coin);
    }
}
