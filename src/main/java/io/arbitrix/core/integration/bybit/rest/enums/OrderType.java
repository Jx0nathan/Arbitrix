package io.arbitrix.core.integration.bybit.rest.enums;

import lombok.Getter;

@Getter
public enum OrderType {
    LIMIT("Limit","limit order"),
    MARKET("Market","market order"),
    ;

    private final String desc;
    private final String code;

    OrderType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
