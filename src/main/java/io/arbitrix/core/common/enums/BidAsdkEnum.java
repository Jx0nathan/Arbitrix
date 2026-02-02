package io.arbitrix.core.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BidAsdkEnum {

    BID("BID"),
    ASK("ASK");

    private final String value;
}
