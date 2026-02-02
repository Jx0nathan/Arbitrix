package io.arbitrix.core.integration.bybit.rest.enums;

import lombok.Getter;

@Getter
public enum OrderSide {
    BUY("Buy"),
    SELL("Sell"),
    ;

    private final String code;

    OrderSide(String code) {
        this.code = code;
    }
}
