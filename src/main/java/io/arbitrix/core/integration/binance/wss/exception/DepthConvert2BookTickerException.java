package io.arbitrix.core.integration.binance.wss.exception;

/**
 * @author mcx
 * @date 2023/11/20
 * @description
 */
public class DepthConvert2BookTickerException extends RuntimeException {
    public DepthConvert2BookTickerException(String message) {
        super(message);
    }
}
