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
@Conditional(ExchangeCondition.class)
public @interface ExchangeConditional {

    /**
     * 交易所名字
     */
    String exchangeName();

}
