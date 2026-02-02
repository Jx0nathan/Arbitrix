package io.arbitrix.core.common.exception;

/**
 * An exception which can occur while invoking methods of the Binance API.
 *
 * @author jonathan.ji
 */
public class BinanceApiException extends RuntimeException {
    /**
     * Error response object returned by Binance API.
     */
    private ApiError error;

    /**
     * Instantiates a new binance api exception.
     *
     * @param error an error response object
     */
    public BinanceApiException(ApiError error) {
        this.error = error;
    }

    /**
     * Instantiates a new binance api exception.
     */
    public BinanceApiException() {
        super();
    }

    /**
     * Instantiates a new binance api exception.
     *
     * @param message the message
     */
    public BinanceApiException(String message) {
        super(message);
    }

    /**
     * Instantiates a new binance api exception.
     *
     * @param cause the cause
     */
    public BinanceApiException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiates a new binance api exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public BinanceApiException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @return the response error object from Binance API, or null if no response object was returned (e.g. server returned 500).
     */
    public ApiError getError() {
        return error;
    }

    @Override
    public String getMessage() {
        if (error != null) {
            return error.getMsg();
        }
        return super.getMessage();
    }
}
