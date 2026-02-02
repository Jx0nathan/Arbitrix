package io.arbitrix.core.integration.bitget.util;

import org.springframework.stereotype.Component;
import io.arbitrix.core.common.enums.OrderStatus;
import io.arbitrix.core.common.enums.ExecutionType;
import io.arbitrix.core.common.enums.OrderSide;
import io.arbitrix.core.common.enums.OrderType;

@Component
public class BitgetUtil {
    public static OrderSide convertSide(String side) {
        return OrderSide.valueOf(side.toUpperCase());
    }

    public static ExecutionType convertExecutionType(String status) {
        if ("init".equalsIgnoreCase(status) || "new".equalsIgnoreCase(status)) {
            return ExecutionType.NEW;
        }
        if ("cancelled".equalsIgnoreCase(status)) {
            return ExecutionType.CANCELED;
        }
        if ("partial-fill".equalsIgnoreCase(status) || "full-fill".equalsIgnoreCase(status)) {
            return ExecutionType.TRADE;
        }
        return null;
    }

    public static String symbolFromBitgetSpotToArbitrix(String symbol) {
        return symbol.replace("_SPBL", "");
    }
    public static String symbolFromArbitrixToBitgetSpot(String symbol) {
        return String.format("%s_%s", symbol.toUpperCase(), "SPBL");
    }

    public static OrderStatus convertStatus(String status) {
        if ("init".equalsIgnoreCase(status) || "new".equalsIgnoreCase(status)) {
            return OrderStatus.NEW;
        }
        if ("cancelled".equalsIgnoreCase(status)) {
            return OrderStatus.CANCELED;
        }
        if ("partial-fill".equalsIgnoreCase(status)) {
            return OrderStatus.PARTIALLY_FILLED;
        }
        if ("full-fill".equalsIgnoreCase(status)) {
            return OrderStatus.FILLED;
        }
        return null;
    }

    public static String convertOrderType(OrderType type) {
        if (type == OrderType.LIMIT || type == OrderType.LIMIT_MAKER){
            return "limit";
        }
        if (type == OrderType.MARKET){
            return "market";
        }
        return null;
    }
}
