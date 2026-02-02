package io.arbitrix.core.facade.account;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import io.arbitrix.core.common.enums.ExchangeNameEnum;
import io.arbitrix.core.common.response.AccountTradeFee;
import io.arbitrix.core.common.response.CoinBalance;
import io.arbitrix.core.integration.okx.rest.OkxAccountClient;
import io.arbitrix.core.integration.okx.rest.dto.req.OkxAccountTradeFeeResponse;
import io.arbitrix.core.utils.ExchangeMarketOpenUtilV2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Log4j2
@Component("account-OKX")
public class OkxAccountAction implements AccountAction {
    private static final String ALL_SYMBOL = "ALL_SYMBOL";

    private final OkxAccountClient accountFeeClient;
    private final ExchangeMarketOpenUtilV2 exchangeMarketOpenUtilV2;

    LoadingCache<String, OkxAccountTradeFeeResponse> cache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(100)
            .build(this::loadAccountTradeFee);

    private OkxAccountTradeFeeResponse loadAccountTradeFee(String symbol) {
        return accountFeeClient.getAccountTradeFee(0, "SPOT", symbol);
    }

    public OkxAccountAction(OkxAccountClient accountFeeClient, ExchangeMarketOpenUtilV2 exchangeMarketOpenUtilV2) {
        this.accountFeeClient = accountFeeClient;
        this.exchangeMarketOpenUtilV2 = exchangeMarketOpenUtilV2;
    }

    @Override
    public List<AccountTradeFee> getAccountTradeFee() {
        List<AccountTradeFee> accountTradeFeeList = new ArrayList<>();
        List<String> symbols = exchangeMarketOpenUtilV2.getMarketTradeInfoByExchange(ExchangeNameEnum.OKX.getValue());
        for (String symbol : symbols) {
            OkxAccountTradeFeeResponse response = cache.get(symbol.toUpperCase());
            AccountTradeFee accountTradeFee = this.conventAccountTradeFee(symbol.toUpperCase(), response);
            if (accountTradeFee != null) {
                accountTradeFeeList.add(accountTradeFee);
            }
        }
        return accountTradeFeeList;
    }

    @Override
    public AccountTradeFee getAccountTradeFeeBySymbol(String symbol) {
        OkxAccountTradeFeeResponse response = cache.get(symbol.toUpperCase());
        return this.conventAccountTradeFee(symbol.toUpperCase(), response);
    }

    @Override
    public List<CoinBalance> getAllCoinBalance() {
        //TODO 未实现
        return Collections.emptyList();
    }

    @Override
    public CoinBalance getCoinBalance(String coin) {
        //TODO 未实现
        return null;
    }

    private AccountTradeFee conventAccountTradeFee(String symbol, OkxAccountTradeFeeResponse accountTradeFeeResponse) {
        if (0 == accountTradeFeeResponse.getCode() && !CollectionUtils.isEmpty(accountTradeFeeResponse.getData())) {
            io.arbitrix.core.integration.okx.rest.dto.req.OkxAccountTradeFee okxAccountTradeFee = accountTradeFeeResponse.getData().get(0);
            return new AccountTradeFee(symbol, okxAccountTradeFee.getMaker(), okxAccountTradeFee.getTaker());
        }
        return null;
    }
}
