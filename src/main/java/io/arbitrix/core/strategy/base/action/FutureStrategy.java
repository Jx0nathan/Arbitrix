package io.arbitrix.core.strategy.base.action;

import io.arbitrix.core.common.domain.FutureOrderExecutionContext;


/**
 * 下单策略
 *
 * @author jonathan.ji
 */
public interface FutureStrategy {

    /**
     * @param context execution params
     *                boost volume based, only place order based on best bid/ask price
     */
    void execute(FutureOrderExecutionContext context);

}
