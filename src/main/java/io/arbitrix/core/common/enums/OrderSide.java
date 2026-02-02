package io.arbitrix.core.common.enums;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author jonathan.ji
 */
@AllArgsConstructor
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public enum OrderSide {
    BUY("BUY"),
    SELL("SELL");

    private final String value;
}
