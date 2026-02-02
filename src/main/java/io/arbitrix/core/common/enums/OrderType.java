package io.arbitrix.core.common.enums;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author jonathan.ji
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public enum OrderType {
    LIMIT,
    MARKET,
    STOP_LOSS,
    STOP_LOSS_LIMIT,
    TAKE_PROFIT,
    TAKE_PROFIT_LIMIT,
    LIMIT_MAKER
}
