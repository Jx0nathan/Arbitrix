package io.arbitrix.core.integration.bybit.rest.enums;


import lombok.Getter;

@Getter
public enum TimeForceEnum {

    GTC("GTC"),

    POST_ONLY("PostOnly"),

    FOK("FOK"),

    IOC("IOC"),

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
