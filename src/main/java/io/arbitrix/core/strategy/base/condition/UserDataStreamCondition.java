package io.arbitrix.core.strategy.base.condition;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author jonathan.ji
 */
@Slf4j
public class UserDataStreamCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        //TODO debug发现这个matches始终返回false, 暂时先用ConditionOnBean代替
        return context.getBeanFactory().containsBean("pureMarketMakingSpotOrderTradeDataManager")
                || context.getBeanFactory().containsBean("profitOrderTradeDataManager");
    }
}
