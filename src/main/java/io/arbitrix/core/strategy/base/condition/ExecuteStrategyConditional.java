package io.arbitrix.core.strategy.base.condition;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author jonathan.ji
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Conditional(ExecuteStrategyCondition.class)
public @interface ExecuteStrategyConditional {

    /**
     * 当前需要执行的哪种做市策略
     */
    String executeStrategyName();

}
