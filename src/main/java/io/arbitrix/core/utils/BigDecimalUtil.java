package io.arbitrix.core.utils;

import java.math.BigDecimal;

public class BigDecimalUtil {

    /**
     * 检查两个订单价格之间的差值是否是给定tickSize
     *
     * @param preOrderPrice 预订单价格
     * @param nextOrderPrice 下一个订单价格
     * @param tickSize      每个tick的大小
     */
    public static boolean isPriceDifferenceNotTickSize(BigDecimal preOrderPrice, BigDecimal nextOrderPrice, BigDecimal tickSize) {
        // 计算价格差值
        BigDecimal priceDifference = nextOrderPrice.subtract(preOrderPrice);
        return priceDifference.abs().compareTo(tickSize) == 0;
    }
}
