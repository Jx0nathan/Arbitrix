package io.arbitrix.core.integration.bybit.rest.exception;

/**
 * @author mcx
 * @date 2023/11/1
 * @description
 */
public class BybitCancelAllOrdersException extends RuntimeException {
    public BybitCancelAllOrdersException(String message) {
        super(message);
    }

    public BybitCancelAllOrdersException(String message, Throwable cause) {
        super(message, cause);
    }

    public BybitCancelAllOrdersException(Throwable cause) {
        super(cause);
    }
}
