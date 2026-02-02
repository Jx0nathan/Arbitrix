package io.arbitrix.core.common.exception;

import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import io.arbitrix.core.integration.binance.constant.BinanceApiConstants;

/**
 * Binance API error object.
 */
@Data
public class ApiError {

    /**
     * Error code.
     */
    private int code;

    /**
     * Error message.
     */
    private String msg;

    @Override
    public String toString() {
        return new ToStringBuilder(this, BinanceApiConstants.TO_STRING_BUILDER_STYLE)
                .append("code", code)
                .append("msg", msg)
                .toString();
    }
}
