package io.arbitrix.core.strategy.base.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @author jonathan.ji
 */
@Getter
@Slf4j
@AllArgsConstructor
public enum ProfitOrderPlaceStrategyEnum {

    /**
     * 基于最优定价上下浮动一定的阈值
     */
    ORDER_LEVEL_SPREAD_BY_BEST_PRICE("1"),

    /**
     * 跟随N档的价格直接出价
     */
    FOLLOW_PRICE_BY_TOP_N("2"),

    /**
     * 卖价基于买一价的浮动
     */
    ASK_PRICE_BASE_ON_BEST_BID_PRICE("3"),

    /**
     * 以USDT设定锚定价
     */
    PRICE_BASE_ON_USDT_PRICE("4");

    private final String strategyTag;

    public static ProfitOrderPlaceStrategyEnum getOrderPlaceStrategy(String strategyTag) {
        for (ProfitOrderPlaceStrategyEnum strategyEnum : ProfitOrderPlaceStrategyEnum.values()) {
            if (Objects.equals(strategyEnum.strategyTag, strategyTag)) {
                return strategyEnum;
            }
        }
        log.warn("If no corresponding strategy is found, use the default strategy. {}", ORDER_LEVEL_SPREAD_BY_BEST_PRICE);
        return ORDER_LEVEL_SPREAD_BY_BEST_PRICE;
    }
}
