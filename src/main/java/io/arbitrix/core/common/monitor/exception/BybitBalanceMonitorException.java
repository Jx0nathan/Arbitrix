package io.arbitrix.core.common.monitor.exception;

import io.arbitrix.core.common.enums.ExchangeNameEnum;

/**
 * @author mcx
 * @date 2023/11/1
 * @description
 */
public class BybitBalanceMonitorException extends BalanceMonitorException {
    public BybitBalanceMonitorException(String message) {
        super(message, ExchangeNameEnum.BYBIT);
    }
}
