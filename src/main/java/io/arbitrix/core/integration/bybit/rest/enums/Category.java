package io.arbitrix.core.integration.bybit.rest.enums;

import lombok.Getter;

@Getter
public enum Category {
    SPOT("spot"),
    LINEAR("linear"),
    INVERSE("inverse"),
    OPTION("option"),
    ;

    private final String code;

    Category(String code) {
        this.code = code;
    }

    public static Category getByCode(String code) {
        for (Category category : Category.values()) {
            if (category.getCode().equalsIgnoreCase(code)) {
                return category;
            }
        }
        return null;
    }

    public static boolean isLinear(String category) {
        return LINEAR.equals(getByCode(category));
    }
}
