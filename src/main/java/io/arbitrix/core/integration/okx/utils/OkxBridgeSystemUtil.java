package io.arbitrix.core.integration.okx.utils;

import org.springframework.stereotype.Component;
import io.arbitrix.core.common.enums.OrderStatus;
import io.arbitrix.core.common.enums.ExecutionType;
import io.arbitrix.core.integration.okx.rest.enums.OkxOrderStatus;
import io.arbitrix.core.integration.okx.rest.enums.OkxSide;
import io.arbitrix.core.common.enums.OrderSide;

@Component
public class OkxBridgeSystemUtil {

    public String convertSymbol(String symbol) {
        return symbol.replace("-", "");
    }

    public OrderStatus convertStatus(OkxOrderStatus state) {
        if (OkxOrderStatus.Live.equals(state)) {
            return OrderStatus.NEW;
        }
        if (OkxOrderStatus.Canceled.equals(state)) {
            return OrderStatus.CANCELED;
        }
        if (OkxOrderStatus.FullyFilled.equals(state)) {
            return OrderStatus.FILLED;
        }
        if (OkxOrderStatus.PartiallyFilled.equals(state)) {
            return OrderStatus.PARTIALLY_FILLED;
        }
        return null;
    }

    public ExecutionType convertExecutionType(String state) {
        if (OkxOrderStatus.Live.name().equalsIgnoreCase(state)) {
            return ExecutionType.NEW;
        }
        if (OkxOrderStatus.Canceled.name().equalsIgnoreCase(state)) {
            return ExecutionType.CANCELED;
        }
        if (OkxOrderStatus.FullyFilled.name().equalsIgnoreCase(state) || OkxOrderStatus.PartiallyFilled.name().equalsIgnoreCase(state)) {
            return ExecutionType.TRADE;
        }
        return null;
    }

    public OrderSide convertSide(OkxSide side) {
        if (OkxSide.BUY.equals(side)) {
            return OrderSide.BUY;
        }
        if (OkxSide.SELL.equals(side)) {
            return OrderSide.SELL;
        }
        return null;
    }

    public OrderSide convertSideStr(String side) {
        if (OkxSide.BUY.name().equalsIgnoreCase(side)) {
            return OrderSide.BUY;
        }
        if (OkxSide.SELL.name().equalsIgnoreCase(side)) {
            return OrderSide.SELL;
        }
        return null;
    }
}
