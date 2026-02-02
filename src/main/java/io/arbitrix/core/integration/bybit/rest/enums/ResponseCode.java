package io.arbitrix.core.integration.bybit.rest.enums;

import lombok.Getter;

@Getter
public enum ResponseCode {
    ORDER_NOT_EXIST("170213", "Order does not exist"),
    INSUFFICIENT_BALANCE("170131", "Insufficient balance."),
    ;

    private final String desc;
    private final String code;

    ResponseCode(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
