package io.arbitrix.core.strategy.base.action;

import io.arbitrix.core.common.domain.SpotOrderExecutionContext;


/**
 * 下单策略
 *
 * @author jonathan.ji
 */
public interface SpotStrategy {

    /**
     * @param context execution params
     *                boost volume based, only place order based on best bid/ask price
     */
    void execute(SpotOrderExecutionContext context);

}
