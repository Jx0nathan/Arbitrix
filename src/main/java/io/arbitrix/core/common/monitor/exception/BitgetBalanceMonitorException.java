package io.arbitrix.core.common.monitor.exception;

import io.arbitrix.core.common.enums.ExchangeNameEnum;

/**
 * @author mcx
 * @date 2023/11/1
 * @description
 */
public class BitgetBalanceMonitorException extends BalanceMonitorException {
    public BitgetBalanceMonitorException(String message) {
        super(message, ExchangeNameEnum.BITGET);
    }
}
