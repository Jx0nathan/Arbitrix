package io.arbitrix.core.strategy.future_pure_market_making.data;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.arbitrix.core.common.enums.ExchangeNameEnum;
import io.arbitrix.core.integration.bybit.rest.BybitPositionClient;
import io.arbitrix.core.integration.bybit.rest.enums.Category;
import io.arbitrix.core.strategy.base.condition.ExecuteStrategyConditional;
import io.arbitrix.core.utils.ExchangeMarketOpenUtilV2;
import io.arbitrix.core.common.util.JacksonUtil;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * @author joanthan.ji
 */
@Component
@ExecuteStrategyConditional(executeStrategyName = "future_pure_market_making")
public class BybitPositionInitialization {

    @Value("${market.making.symbol.leverage:}")
    private String symbolLeverage;

    private final BybitPositionClient bybitPositionClient;
    private final ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil;

    public BybitPositionInitialization(BybitPositionClient bybitPositionClient, ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil) {
        this.bybitPositionClient = bybitPositionClient;
        this.exchangeMarketOpenUtil = exchangeMarketOpenUtil;
    }

    @PostConstruct
    public void init() {
        List<String> symbolList = exchangeMarketOpenUtil.getSymbolPairs(ExchangeNameEnum.BYBIT);
        Map<String, Object> leverage = JacksonUtil.fromMap(symbolLeverage);
        symbolList.forEach(symbol -> {
            String buyOrSellLeverage = String.valueOf(leverage.get(symbol));
            bybitPositionClient.setLeverage(Category.LINEAR.getCode(), symbol, buyOrSellLeverage, buyOrSellLeverage);
        });
    }
}
