package io.arbitrix.core.strategy.base.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author jonathan.ji
 */
@Getter
@Slf4j
@AllArgsConstructor
public enum FutureExecuteStrategyEnum {

    FUTURE_PURE_LONG("future_pure_long"),

    FUTURE_PURE_SHORT("future_pure_short"),

    PRICE_CHANGE_ON_BEST_PRICE("1");

    private final String strategyTag;

    /**
     * 系统配置的策略名称
     */
    public static final String FUTURE_PURE_MARKET_MAKING_STRATEGY = "future_pure_market_making_strategy";

}
