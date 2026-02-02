package io.arbitrix.core.strategy.base.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 当前系统允许允许的策略
 *
 * @author jonathan.ji
 */
@Getter
@AllArgsConstructor
public enum ApplicationExecuteStrategyEnum {

    /**
     * original single-pair market making strategy for generating trading volume
     */
    PURE_MARKET_MAKING("pure_market_making"),

    /**
     * single-pair market making strategy for generating profit
     */
    PROFIT_MARKET_MAKING("profit_market_making"),

    /**
     * future original single-pair market making strategy for generating trading volume
     */
    FUTURE_PURE_MARKET_MAKING("future_pure_market_making");

    private final String strategyName;

}
