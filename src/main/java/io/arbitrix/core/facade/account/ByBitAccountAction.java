package io.arbitrix.core.facade.account;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import io.arbitrix.core.common.enums.ExchangeNameEnum;
import io.arbitrix.core.common.response.AccountTradeFee;
import io.arbitrix.core.common.response.CoinBalance;
import io.arbitrix.core.integration.bybit.rest.api.BybitAccountApi;
import io.arbitrix.core.integration.bybit.rest.dto.res.BaseRestRes;
import io.arbitrix.core.integration.bybit.rest.dto.res.FeeRateRes;
import io.arbitrix.core.integration.bybit.rest.dto.res.WalletBalanceRes;
import io.arbitrix.core.integration.bybit.rest.enums.Category;
import io.arbitrix.core.utils.ExchangeMarketOpenUtilV2;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Log4j2
@Component("account-BYBIT")
public class ByBitAccountAction implements AccountAction {
    private static final String ALL_SYMBOL = "ALL_SYMBOL";
    private final BybitAccountApi bybitAccountApi;
    private final ExchangeMarketOpenUtilV2 exchangeMarketOpenUtilV2;

    LoadingCache<String, FeeRateRes> cache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(100)
            .build(this::loadAccountTradeFee);

    private FeeRateRes loadAccountTradeFee(String symbol) {
        BaseRestRes<FeeRateRes> feeRateResBaseRestRes = null;
        if (ALL_SYMBOL.equals(symbol)) {
            feeRateResBaseRestRes = bybitAccountApi.getTradeFee(Category.SPOT.getCode(), null, null);
        } else {
            feeRateResBaseRestRes = bybitAccountApi.getTradeFee(Category.SPOT.getCode(), symbol, null);
        }
        if (feeRateResBaseRestRes.isSuccess()) {
            return feeRateResBaseRestRes.getResult();
        }
        log.error("exchange: {}, get trade fee error, {}", ExchangeNameEnum.BYBIT.getValue(), feeRateResBaseRestRes);
        return null;
    }

    public ByBitAccountAction(BybitAccountApi bybitAccountApi, ExchangeMarketOpenUtilV2 exchangeMarketOpenUtilV2) {
        this.bybitAccountApi = bybitAccountApi;
        this.exchangeMarketOpenUtilV2 = exchangeMarketOpenUtilV2;
    }

    @Override
    public List<AccountTradeFee> getAccountTradeFee() {
        List<String> symbols = exchangeMarketOpenUtilV2.getMarketTradeInfoByExchange(ExchangeNameEnum.BYBIT.getValue());
        if (CollectionUtils.isEmpty(symbols)) {
            log.warn("symbols is empty, exchange: {}", ExchangeNameEnum.BYBIT.getValue());
            return Collections.emptyList();
        }
        FeeRateRes feeRateRes = cache.get(ALL_SYMBOL);
        if (Objects.isNull(feeRateRes)) {
            log.warn("exchange: {}, fee rate is null", ExchangeNameEnum.BYBIT.getValue());
            return Collections.emptyList();
        }
        return feeRateRes.convert2AccountTradeFeesBySymbols(symbols);
    }

    @Override
    public AccountTradeFee getAccountTradeFeeBySymbol(String symbol) {
        FeeRateRes feeRateRes = cache.get(symbol.toUpperCase());
        if (Objects.isNull(feeRateRes)) {
            log.warn("fee rate is null, symbol: {}", symbol.toUpperCase());
            return null;
        }
        return feeRateRes.convert2SingleAccountTradeFee();
    }

    @Override
    public List<CoinBalance> getAllCoinBalance() {
        BaseRestRes<WalletBalanceRes> walletBalance = bybitAccountApi.getWalletBalance(null, null);
        if (walletBalance.isSuccess()) {
            return walletBalance.getResult().convert2CoinBalances();
        }
        log.error("get all coin balance error, {}", walletBalance);
        return null;
    }

    @Override
    public CoinBalance getCoinBalance(String coin) {
        BaseRestRes<WalletBalanceRes> walletBalance = bybitAccountApi.getWalletBalance(null, coin);
        if (walletBalance.isSuccess()) {
            // bybit 在获取单个币种的时候，返回的是一个数组，所以这里取第一个
            return walletBalance.getResult().convert2CoinBalances().get(0);
        }
        log.error("get coin balance error, {},coin: {}", walletBalance, coin);
        return null;
    }
}
