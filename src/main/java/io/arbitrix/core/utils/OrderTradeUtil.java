package io.arbitrix.core.utils;

import org.apache.commons.lang3.tuple.Pair;
import io.arbitrix.core.common.enums.OrderSide;

/**
 * @author jonathan.ji
 */
public class OrderTradeUtil {
    private static final String ORDER_TRADE_KEY_SEPARATE = "#";

    public static String buildOrderTradeKey(String exchangeName, String symbol, OrderSide orderSide) {
        return (exchangeName.toUpperCase() + ORDER_TRADE_KEY_SEPARATE + symbol.toUpperCase() + ORDER_TRADE_KEY_SEPARATE + orderSide.toString()).toUpperCase();
    }

    public static String buildLevelOrderTradeKey(String exchangeName, String symbol, OrderSide orderSide, Integer level) {
        return (exchangeName.toUpperCase() + ORDER_TRADE_KEY_SEPARATE + symbol.toUpperCase() + ORDER_TRADE_KEY_SEPARATE + orderSide.toString()).toUpperCase() + ORDER_TRADE_KEY_SEPARATE + level;
    }

    public static String buildOrderTradeKeyForStr(String symbol, String orderSide) {
        return (symbol + ORDER_TRADE_KEY_SEPARATE + orderSide.toUpperCase());
    }

    public static Pair<String, String> parseOrderTradeKey(String orderTradeKey) {
        String[] split = orderTradeKey.split(ORDER_TRADE_KEY_SEPARATE);
        return Pair.of(split[0], split[1]);
    }
}
