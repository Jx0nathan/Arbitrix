package io.arbitrix.core.common.monitor.aop;

import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import io.arbitrix.core.common.monitor.utils.MetricsUtils;
import io.arbitrix.core.common.monitor.annotation.DisablePercentilesMetrics;

import java.time.Duration;


@Aspect
@Log4j2
public class PercentilesMetricsAspect {

    @Pointcut("@annotation(io.arbitrix.core.common.monitor.annotation.PercentilesMetrics) || @within(io.arbitrix.core.common.monitor.annotation.PercentilesMetrics)")
    public void methodOrClassMonitorEnabledPointcut() {
    }

    @AfterThrowing(pointcut = "methodOrClassMonitorEnabledPointcut()", throwing = "t")
    public void onException(JoinPoint joinPoint, Throwable t) {
        log.warn("【" + joinPoint.getSignature().getName() + "】", t);
    }

    @Around("methodOrClassMonitorEnabledPointcut()")
    public Object metrics(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object result;
        MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
        boolean disableMetrics = methodSignature.getMethod().getAnnotation(DisablePercentilesMetrics.class) != null
                || proceedingJoinPoint.getTarget().getClass().getAnnotation(DisablePercentilesMetrics.class) != null;
        StopWatch timer = new StopWatch();
        try {
            timer.start();
            result = proceedingJoinPoint.proceed();
        } finally {
            timer.stop();
            if (!disableMetrics) {
                try {
                    String className = proceedingJoinPoint.getTarget().getClass().getSimpleName();
                    String methodName = className + "." + methodSignature.getName() + "()";
                    String[] tags = {"method", methodName, "url", this.getRequestUrl()};
                    MetricsUtils.recordTimeDefaultPercentiles("percentiles_method_with_url_timer", "method with url timer", Duration.ofMillis(timer.getTotalTimeMillis()), tags);
                    MetricsUtils.count("percentiles_method_with_url_count", "method with url count", tags);
                } catch (Throwable e) {
                    log.error("Exception occurred in after-proceed logic");
                }
            }else {
                log.debug("metrics disabled, method:{}", methodSignature.getName());
            }
        }
        return result;
    }

    private String getRequestUrl() {
        try {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes instanceof ServletRequestAttributes) {
                return ((ServletRequestAttributes) requestAttributes).getRequest().getRequestURL().toString();
            }
        } catch (Exception ignore) {
        }
        return "None";
    }

}