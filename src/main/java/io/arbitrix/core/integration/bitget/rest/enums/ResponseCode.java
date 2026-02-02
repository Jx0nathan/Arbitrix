package io.arbitrix.core.integration.bitget.rest.enums;

import lombok.Getter;

@Getter
public enum ResponseCode {
    SUCCESS("00000", "success"),
    ORDER_NOT_EXIST("43001", "Order does not exist"),
    INSUFFICIENT_BALANCE("43012", "Insufficient balance."),
    ;

    private final String desc;
    private final String code;

    ResponseCode(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
