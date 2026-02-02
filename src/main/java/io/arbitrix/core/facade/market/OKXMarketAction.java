package io.arbitrix.core.facade.market;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import io.arbitrix.core.common.domain.SymbolLimitInfo;
import io.arbitrix.core.common.event.BookTickerEvent;
import io.arbitrix.core.integration.bybit.rest.enums.Category;
import io.arbitrix.core.integration.okx.rest.OkxPublicDataClient;
import io.arbitrix.core.integration.okx.rest.dto.req.ServerTime;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Log4j2
@Component("market-OKX")
public class OKXMarketAction implements MarketAction {
    private final OkxPublicDataClient okxPublicDataClient;

    LoadingCache<String, SymbolLimitInfo> cache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.DAYS)
            .maximumSize(100)
            .build(this::loadAccountTradeFee);

    private SymbolLimitInfo loadAccountTradeFee(String key) {
        // TODO 2023/11/7 后面改为从rest 接口获取
        // /api/v5/public/instruments
        // basePrecision : lotSz
        // quotePrecision : tickSz
        // minOrderQty : minSz
        // maxOrderQty : maxLmtSz
        switch (key) {
            case "spot-BTC-USDT":
                return SymbolLimitInfo.builder().symbol("BTC-USDT")
                        .category(Category.SPOT.getCode())
                        .basePrecision("0.00000001")
                        .quotePrecision("0.1")
                        .minOrderQty("0.00001")
                        .maxOrderQty("9999999999")
                        .build();
            case "spot-ETH-USDT":
                return SymbolLimitInfo.builder().symbol("ETH-USDT")
                        .category(Category.SPOT.getCode())
                        .basePrecision("0.000001")
                        .quotePrecision("0.01")
                        .minOrderQty("0.0001")
                        .maxOrderQty("999999999999")
                        .build();
            case "spot-BTC-USDC":
                return SymbolLimitInfo.builder().symbol("BTC-USDC")
                        .category(Category.SPOT.getCode())
                        .basePrecision("0.00000001")
                        .quotePrecision("0.1")
                        .minOrderQty("0.00001")
                        .maxOrderQty("9999999999")
                        .build();
            default:
                return null;
        }
    }

    public OKXMarketAction(OkxPublicDataClient okxPublicDataClient) {
        this.okxPublicDataClient = okxPublicDataClient;
    }
    @Override
    public SymbolLimitInfo getSymbolLimitInfo(String symbol, String category) {
        return cache.get(symbolLimitInfoCacheKey(symbol, category));
    }

    @Override
    public String getServerTime() {
        ServerTime serverTime = this.okxPublicDataClient.getServerTime();
        if (Objects.isNull(serverTime)) {
            return null;
        }
        return serverTime.getTs();
    }

    @Override
    public BookTickerEvent lastTicker(String symbol) {
        // TODO 2024/3/21 待实现
        return null;
    }
}
