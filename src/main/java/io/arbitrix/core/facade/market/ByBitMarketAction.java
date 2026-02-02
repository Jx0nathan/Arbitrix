package io.arbitrix.core.facade.market;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import io.arbitrix.core.common.domain.SymbolLimitInfo;
import io.arbitrix.core.common.event.BookTickerEvent;
import io.arbitrix.core.integration.bybit.rest.BybitMarketRestClient;
import io.arbitrix.core.integration.bybit.rest.dto.res.ServerTimeRes;
import io.arbitrix.core.integration.bybit.rest.enums.Category;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Log4j2
@Component("market-BYBIT")
public class ByBitMarketAction implements MarketAction {
    private final BybitMarketRestClient bybitMarketRestClient;
    private final Map<String, BookTickerEvent> bookTickerEventMap = new ConcurrentHashMap<>();
    LoadingCache<String, SymbolLimitInfo> cache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.DAYS)
            .maximumSize(100)
            .build(this::loadAccountTradeFee);

    private SymbolLimitInfo loadAccountTradeFee(String key) {
        // TODO 2023/11/7 后面改为从rest 接口获取
        switch (key) {
            case "spot-BTCUSDT":
                return SymbolLimitInfo.builder().symbol("BTCUSDT")
                        .category(Category.SPOT.getCode())
                        .basePrecision("0.000001")
                        .quotePrecision("0.00000001")
                        .minOrderQty("0.000048")
                        .maxOrderQty("71.73956243")
                        .build();
            case "spot-ETHUSDT":
                return SymbolLimitInfo.builder().symbol("ETHUSDT")
                        .category(Category.SPOT.getCode())
                        .basePrecision("0.00001")
                        .quotePrecision("0.0000001")
                        .minOrderQty("0.00062")
                        .maxOrderQty("1229.2336343")
                        .build();
            case "spot-BTCUSDC":
                return SymbolLimitInfo.builder().symbol("BTCUSDC")
                        .category(Category.SPOT.getCode())
                        .basePrecision("0.000001")
                        .quotePrecision("0.00000001")
                        .minOrderQty("0.000198")
                        .maxOrderQty("71.71029043")
                        .build();
            default:
                return null;
        }
    }

    public ByBitMarketAction(BybitMarketRestClient bybitMarketRestClient) {
        this.bybitMarketRestClient = bybitMarketRestClient;
    }

    @Override
    public SymbolLimitInfo getSymbolLimitInfo(String symbol, String category) {
        return cache.get(symbolLimitInfoCacheKey(symbol, category));
    }

    @Override
    public String getServerTime() {
        ServerTimeRes serverTimeRes = bybitMarketRestClient.serverTime();
        if (Objects.isNull(serverTimeRes)) {
            return null;
        }
        return serverTimeRes.milliSecondStr();
    }

    @Override
    public BookTickerEvent lastTicker(String symbol) {
        return bookTickerEventMap.get(symbol);
    }

    public void onTickerEvent(BookTickerEvent bookTickerEvent) {
        if (Objects.isNull(bookTickerEvent)) {
            log.warn("ByBitMarketAction.onTickerEvent.bookTickerEvent is null, ignore");
            return;
        }
        // 不为空表示有更新,直接覆盖旧有的值
        bookTickerEventMap.put(bookTickerEvent.getSymbol(), bookTickerEvent);
    }
}
