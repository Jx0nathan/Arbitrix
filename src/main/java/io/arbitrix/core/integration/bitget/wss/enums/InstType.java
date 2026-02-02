package io.arbitrix.core.integration.bitget.wss.enums;

import lombok.Getter;

@Getter
public enum InstType {
    SP("SP", "币公共频道"),
    SPBL("spbl", "币币私有频道"),
    ;

    private final String desc;
    private final String code;

    InstType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
