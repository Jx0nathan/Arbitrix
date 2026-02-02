package io.arbitrix.core.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import io.arbitrix.core.common.enums.ExchangeNameEnum;
import io.arbitrix.core.common.domain.MarketOpenExchangeInfo;
import io.arbitrix.core.common.util.JacksonUtil;

import java.util.List;
import java.util.Map;

/**
 * v1版本：针对一个服务器跑所有的交易所，所有的交易对
 *
 * 配置：
 *
 * {
 *     "exchangeList": [
 *         "OK",
 *         "BITGET"
 *     ],
 *     "symbolInfoMap": {
 *         "BINANCE": [
 *             "BTCUSDT"
 *         ],
 *         "OKX": [
 *             "ETH-USDT"
 *         ],
 *         "BITGET": [
 *             "BTCUSDT"
 *         ]
 *     }
 * }
 *
 * @author jonathan.ji
 */
@Component
public class ExchangeMarketOpenUtilV1 {

    @Value("${market.open.exchange.info:}")
    private String marketOpenExchangeInfoStr;

    public boolean binanceMarketOpen() {
        return exchangeMarkerOpen(ExchangeNameEnum.BINANCE.name());
    }

    public List<String> getBinanceSymbolPairs() {
        return this.getMarketOpenExchangeInfo().getSymbolInfoMap().get(ExchangeNameEnum.BINANCE.name());
    }

    public List<String> getOkxSymbolPairs() {
        return this.getMarketOpenExchangeInfo().getSymbolInfoMap().get(ExchangeNameEnum.OKX.name());
    }

    public boolean okxMarketOpen() {
        return exchangeMarkerOpen(ExchangeNameEnum.OKX.name());
    }

    public boolean exchangeMarkerOpen(String exchangeName) {
        List<String> exchangeList = this.getMarketOpenExchangeInfo().getExchangeList();
        return !CollectionUtils.isEmpty(exchangeList) && exchangeList.contains(exchangeName);
    }

    public Map<String, List<String>> getSymbolInfoMap() {
        return this.getMarketOpenExchangeInfo().getSymbolInfoMap();
    }

    private MarketOpenExchangeInfo getMarketOpenExchangeInfo() {
        return JacksonUtil.from(marketOpenExchangeInfoStr, MarketOpenExchangeInfo.class);
    }
}
