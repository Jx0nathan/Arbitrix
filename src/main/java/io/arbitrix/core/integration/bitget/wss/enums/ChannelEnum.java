package io.arbitrix.core.integration.bitget.wss.enums;

import lombok.Getter;

@Getter
public enum ChannelEnum {
    TICKER("ticker", "book ticker"),
    BOOKS("books", "depth channel"),
    ORDERS("orders", "order info channel"),
    ;

    private final String desc;
    private final String code;

    ChannelEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
