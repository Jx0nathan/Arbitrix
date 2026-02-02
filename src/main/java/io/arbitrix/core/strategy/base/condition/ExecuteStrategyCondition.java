package io.arbitrix.core.strategy.base.condition;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author jonathan.ji
 */
@Slf4j
public class ExecuteStrategyCondition implements Condition {
    private static final String APPLICATION_EXECUTE_STRATEGY_NAME = "application_execute_strategy_name";

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        try {
            String executeStrategyParamsName = (String) metadata.getAnnotationAttributes(ExecuteStrategyConditional.class.getName()).get("executeStrategyName");
            Environment environment = context.getEnvironment();
            String executeStrategyName = environment.getProperty(APPLICATION_EXECUTE_STRATEGY_NAME);
            return executeStrategyParamsName.equalsIgnoreCase(executeStrategyName);
        } catch (Exception e) {
           log.error("ExecuteStrategyCondition error", e);
        }
        return false;
    }
}
