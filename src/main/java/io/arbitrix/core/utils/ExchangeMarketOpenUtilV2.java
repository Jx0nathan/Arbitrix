package io.arbitrix.core.utils;

import io.arbitrix.core.common.util.EnvUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import io.arbitrix.core.common.enums.ExchangeNameEnum;
import io.arbitrix.core.common.util.JacksonUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * v2版本：为了更好的执行交易策略，将交易所和交易对分开执行
 * <p>
 * 每个实例的启动命令会指明交易所和交易对
 * (1) -Dexchange=OKX
 * (2) -DsymbolPairs=ETH-USDT
 *
 * @author jonathan.ji
 */
@Component
public class ExchangeMarketOpenUtilV2 {
    public static final String EXCHANGE = "exchange";
    public static final String SIDE_TYPE = "sideType";
    private static final String SYMBOl_PAIR = "symbolPairs";

    @Value("${market.trade.info:}")
    private String marketTradeInfoStr;

    /**
     * 获取支持的交易对 <br>
     * (1) 获取当前实例支持的交易所，比如Binance <br>
     * (2) 获取当前实例支持的交易对，比如ETHUSDT,BTCUSDT <br>
     * (3) 判断当前交易所是否开市，当前币对是否开市 <br>
     */
    public List<String> getSymbolPairs(ExchangeNameEnum exchangeName) {
        if (!exchangeMarkerOpen(exchangeName)) {
            return new ArrayList<>();
        }
        List<String> symbolPairList = this.getSupportSymbolPairs();
        List<String> targetSymbolPairList = new ArrayList<>();
        for (String item : symbolPairList) {
            boolean exchangeOpen = this.exchangeMarkerOpen(exchangeName.name(), item);
            if (exchangeOpen) {
                targetSymbolPairList.add(item);
            }
        }
        return targetSymbolPairList;
    }

    public boolean exchangeCanStart(ExchangeNameEnum exchangeName) {
        List<String> symbolPairs = getSymbolPairs(exchangeName);
        return !CollectionUtils.isEmpty(symbolPairs);
    }

    /**
     * note: 因为每个实例只能启动一个交易所，所以这里只返回一个交易所,不考虑多个交易所的情况
     *
     * @return
     */
    public ExchangeNameEnum getExchange() {
        String exchangeName = EnvUtil.getProperty(EXCHANGE);
        return ExchangeNameEnum.getExchangeName(exchangeName);
    }

    /**
     * 判断交易所是否开启
     */
    public boolean exchangeMarkerOpen(ExchangeNameEnum exchangeName) {
        String startUpExchangeName = EnvUtil.getProperty(EXCHANGE);
        if (startUpExchangeName != null && !startUpExchangeName.isEmpty() && !startUpExchangeName.equalsIgnoreCase(exchangeName.name())) {
            return false;
        }
        return true;
    }

    /**
     * 判断交易所的某个币对是否开启
     */
    public boolean exchangeMarkerOpen(String exchangeName, String symbolPair) {
        Map<String, List<String>> marketTradeInfo = this.getMarketTradeInfo();
        if (CollectionUtils.isEmpty(marketTradeInfo)) {
            return false;
        }
        List<String> exchangeSymbolPairList = marketTradeInfo.get(exchangeName);
        if (CollectionUtils.isEmpty(exchangeSymbolPairList)) {
            return false;
        }
        return exchangeSymbolPairList.contains(symbolPair);
    }

    private List<String> getSupportSymbolPairs() {
        String symbolPair = EnvUtil.getProperty(SYMBOl_PAIR);
        if (symbolPair != null && !symbolPair.isEmpty()) {
            return new ArrayList<>() {{
                add(symbolPair);
            }};
        }
        return new ArrayList<>();
    }

    public Map<String, List<String>> getMarketTradeInfo() {
        Map<String, String> marketTradeInfo = JacksonUtil.fromMap(marketTradeInfoStr, String.class);
        Map<String, List<String>> marketTrade = new HashMap<>();
        marketTradeInfo.forEach((exchangeName, symbolPairStr) -> {
            String[] array = symbolPairStr.split(",");
            List<String> symbolPairList = Arrays.asList(array);
            marketTrade.put(exchangeName.toUpperCase(), symbolPairList);
        });
        return marketTrade;
    }

    public List<String> getMarketTradeInfoByExchange(String exchange) {
        Map<String, List<String>> marketTradeInfo = getMarketTradeInfo();
        if (CollectionUtils.isEmpty(marketTradeInfo)) {
            return Collections.emptyList();
        }
        List<String> marketTrade = marketTradeInfo.get(exchange);
        if (CollectionUtils.isEmpty(marketTrade)) {
            return Collections.emptyList();
        }
        return marketTrade.stream().map(String::toUpperCase).collect(Collectors.toList());
    }

    public static boolean checkSideType(String targetSideType, String expectedSideType) {
        if (StringUtils.isEmpty(targetSideType)) {
          return true;
        }
        return targetSideType.equalsIgnoreCase(expectedSideType);
    }
}
