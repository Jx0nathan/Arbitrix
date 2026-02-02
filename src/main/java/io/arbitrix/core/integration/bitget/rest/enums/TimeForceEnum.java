package io.arbitrix.core.integration.bitget.rest.enums;


import lombok.Getter;

@Getter
public enum TimeForceEnum {

    NORMAL("normal"),

    POST_ONLY("postOnly"),

    FOK("fok"),

    IOC("ioc"),

    ;

    private final String code;

    TimeForceEnum(String code) {
        this.code = code;
    }

    public static TimeForceEnum toEnum(String code) {
        for (TimeForceEnum item : TimeForceEnum.values()) {
            if (item.code.equalsIgnoreCase(code)) {
                return item;
            }
        }
        return null;
    }
}
