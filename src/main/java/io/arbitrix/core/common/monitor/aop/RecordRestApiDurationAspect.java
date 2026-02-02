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
import io.arbitrix.core.common.monitor.RestMonitor;
import io.arbitrix.core.common.monitor.annotation.DisableRecordRestApiDelay;
import io.arbitrix.core.common.monitor.rest.MonitorableResponse;

import java.time.Duration;
import java.util.Objects;

@Aspect
@Log4j2
public class RecordRestApiDurationAspect {

    @Pointcut("@annotation(io.arbitrix.core.common.monitor.annotation.RecordRestApiDuration) || @within(io.arbitrix.core.common.monitor.annotation.RecordRestApiDuration)")
    public void methodOrClassMonitorEnabledPointcut() {
    }

    @AfterThrowing(pointcut = "methodOrClassMonitorEnabledPointcut()", throwing = "t")
    public void onException(JoinPoint joinPoint, Throwable t) {
        log.warn("【" + joinPoint.getSignature().getName() + "】", t);
    }

    @Around("methodOrClassMonitorEnabledPointcut()")
    public Object metrics(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object result = null;
        MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
        boolean disableMetrics = methodSignature.getMethod().getAnnotation(DisableRecordRestApiDelay.class) != null
                || proceedingJoinPoint.getTarget().getClass().getAnnotation(DisableRecordRestApiDelay.class) != null;
        StopWatch timer = new StopWatch();
        timer.start();
        Long startTime = System.currentTimeMillis();
        try {
            result = proceedingJoinPoint.proceed();
        } finally {
            Long receiveTime = System.currentTimeMillis();
            timer.stop();
            try {
                String className = methodSignature.getDeclaringType().getSimpleName();
                String methodName = className + "." + methodSignature.getName() + "()";
                if (disableMetrics) {
                    log.debug("metrics disabled, method:{}", methodName);
                } else if (Objects.isNull(result) || !(result instanceof MonitorableResponse)) {
                    log.debug("result is null or not MonitorableResponse, method:{}", methodName);
                } else {
                    RestMonitor.recordSendingDuration(methodName, (MonitorableResponse) result, startTime);
                    RestMonitor.recordReceiveDelay(methodName, (MonitorableResponse) result, receiveTime);
                    RestMonitor.recordDuration(methodName, (MonitorableResponse) result, Duration.ofMillis(timer.getTotalTimeMillis()));
                }
            } catch (Throwable e) {
                log.error("Exception occurred in after-proceed logic");
            }
        }
        return result;
    }
}
