package io.arbitrix.core.integration.okx.utils;

import io.arbitrix.core.integration.okx.rest.enums.OkxSide;

public class OkxUtil {

    public static OkxSide convertToOrderSide(String side) {
        if ("buy".equalsIgnoreCase(side)) {
            return OkxSide.BUY;
        }
        if ("sell".equalsIgnoreCase(side)) {
            return OkxSide.SELL;
        }
        return null;
    }
}
