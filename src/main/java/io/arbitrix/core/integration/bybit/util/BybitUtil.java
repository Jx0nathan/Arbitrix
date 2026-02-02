package io.arbitrix.core.integration.bybit.util;

import org.springframework.stereotype.Component;
import io.arbitrix.core.common.enums.OrderStatus;
import io.arbitrix.core.common.enums.ExecutionType;
import io.arbitrix.core.common.enums.OrderSide;
import io.arbitrix.core.common.enums.OrderType;

@Component
public class BybitUtil {
    public static OrderSide convertSideIn(String side) {
        return OrderSide.valueOf(side.toUpperCase());
    }

    public static String convertSideOut(OrderSide side) {
        if (OrderSide.BUY.equals(side)) {
            return io.arbitrix.core.integration.bybit.rest.enums.OrderSide.BUY.getCode();
        } else if (OrderSide.SELL.equals(side)) {
            return io.arbitrix.core.integration.bybit.rest.enums.OrderSide.SELL.getCode();
        }
        return null;
    }

    public static ExecutionType convertExecutionType(String bybitExecutionType) {
        if ("Trade".equalsIgnoreCase(bybitExecutionType)) {
            return ExecutionType.TRADE;
        }
        return null;
    }

    public static OrderStatus convertStatus(String status) {
        if ("Created".equalsIgnoreCase(status) || "New".equalsIgnoreCase(status)) {
            return OrderStatus.NEW;
        }
        if ("Cancelled".equalsIgnoreCase(status)) {
            return OrderStatus.CANCELED;
        }
        if ("PartiallyFilled".equalsIgnoreCase(status)) {
            return OrderStatus.PARTIALLY_FILLED;
        }
        if ("Filled".equalsIgnoreCase(status)) {
            return OrderStatus.FILLED;
        }
        return null;
    }

    public static String convertOrderType(OrderType type) {
        if (type == OrderType.LIMIT || type == OrderType.LIMIT_MAKER) {
            return "Limit";
        }
        if (type == OrderType.MARKET) {
            return "Market";
        }
        return null;
    }
}
