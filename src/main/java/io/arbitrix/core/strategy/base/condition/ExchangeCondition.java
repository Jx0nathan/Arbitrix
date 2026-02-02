package io.arbitrix.core.strategy.base.condition;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;

/**
 * @author jonathan.ji
 */
@Slf4j
public class ExchangeCondition implements Condition {
    public static final String EXCHANGE = "exchange";

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        try {
            Map<String, Object> conditionalParamsMap = metadata.getAnnotationAttributes(ExchangeConditional.class.getName());
            String exchangeParamsName = (String) conditionalParamsMap.get("exchangeName");

            Environment environment = context.getEnvironment();
            String exchangeName = environment.getProperty(EXCHANGE);
            return exchangeParamsName.equalsIgnoreCase(exchangeName);
        } catch (Exception e) {
           log.error("ExchangeCondition error", e);
        }
        return false;
    }
}
