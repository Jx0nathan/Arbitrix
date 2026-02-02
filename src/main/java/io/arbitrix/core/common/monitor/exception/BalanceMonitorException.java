package io.arbitrix.core.common.monitor.exception;

import io.arbitrix.core.common.enums.ExchangeNameEnum;

/**
 * @author mcx
 * @date 2023/11/1
 * @description
 */
public class BalanceMonitorException extends RuntimeException {
    private final ExchangeNameEnum exchange;

    public BalanceMonitorException(String message, ExchangeNameEnum exchange) {
        super(message);
        this.exchange = exchange;
    }

    public static BalanceMonitorException of(String message, ExchangeNameEnum exchange) {
        if (ExchangeNameEnum.BYBIT.equals(exchange)) {
            return new BybitBalanceMonitorException(message);
        } else if (ExchangeNameEnum.BINANCE.equals(exchange)) {
            return new BinanceBalanceMonitorException(message);
        } else if (ExchangeNameEnum.OKX.equals(exchange)) {
            return new OkxBalanceMonitorException(message);
        } else if (ExchangeNameEnum.BITGET.equals(exchange)) {
            return new BitgetBalanceMonitorException(message);
        } else {
            return new BalanceMonitorException(message, exchange);
        }
    }

    @Override
    public String toString() {
        return "BalanceMonitorException{" +
                "exchange=" + exchange +
                ", message=" + getMessage() +
                '}';
    }
}
