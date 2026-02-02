package io.arbitrix.core.facade.market;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import io.arbitrix.core.common.domain.SymbolLimitInfo;
import io.arbitrix.core.common.event.BookTickerEvent;
import io.arbitrix.core.integration.binance.rest.BinanceMarketClient;
import io.arbitrix.core.integration.binance.rest.dto.res.ServerTimeResponse;
import io.arbitrix.core.integration.bybit.rest.enums.Category;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Log4j2
@Component("market-BINANCE")
public class BinanceMarketAction implements MarketAction {
    private final BinanceMarketClient binanceMarketClient;

    LoadingCache<String, SymbolLimitInfo> cache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.DAYS)
            .maximumSize(100)
            .build(this::loadAccountTradeFee);

    private SymbolLimitInfo loadAccountTradeFee(String key) {
        // TODO 2023/11/7 后面改为从rest 接口获取
        // /api/v3/exchangeInfo
        // basePrecision : filters -> LOT_SIZE -> stepSize(stripTrailingZeros)
        // quotePrecision : filters -> PRICE_FILTER -> tickSize(stripTrailingZeros)
        // minOrderQty : filters -> LOT_SIZE -> minQty
        // maxOrderQty : filters -> LOT_SIZE -> maxQty
        switch (key) {
            case "spot-BTCUSDT":
                return SymbolLimitInfo.builder().symbol("BTCUSDT")
                        .category(Category.SPOT.getCode())
                        .basePrecision("0.00001")
                        .quotePrecision("0.01")
                        .minOrderQty("0.00001000")
                        .maxOrderQty("9000.00000000")
                        .build();
            case "spot-ETHUSDT":
                return SymbolLimitInfo.builder().symbol("ETHUSDT")
                        .category(Category.SPOT.getCode())
                        .basePrecision("0.0001")
                        .quotePrecision("0.01")
                        .minOrderQty("0.00010000")
                        .maxOrderQty("9000.00000000")
                        .build();
            case "spot-BTCUSDC":
                return SymbolLimitInfo.builder().symbol("BTCUSDC")
                        .category(Category.SPOT.getCode())
                        .basePrecision("0.00001")
                        .quotePrecision("0.01")
                        .minOrderQty("0.00001000")
                        .maxOrderQty("9000.00000000")
                        .build();
            default:
                return null;
        }
    }

    public BinanceMarketAction(BinanceMarketClient binanceMarketClient) {
        this.binanceMarketClient = binanceMarketClient;
    }
    @Override
    public SymbolLimitInfo getSymbolLimitInfo(String symbol, String category) {
        return cache.get(symbolLimitInfoCacheKey(symbol, category));
    }

    @Override
    public String getServerTime() {
        ServerTimeResponse serverTimeResponse = binanceMarketClient.serverTime();
        if (Objects.isNull(serverTimeResponse)) {
            return null;
        }
        return serverTimeResponse.getServerTime().toString();
    }

    @Override
    public BookTickerEvent lastTicker(String symbol) {
        // TODO 2024/3/21 待实现
        return null;
    }
}
