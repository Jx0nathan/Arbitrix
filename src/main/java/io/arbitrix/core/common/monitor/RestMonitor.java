package io.arbitrix.core.common.monitor;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import io.arbitrix.core.common.monitor.utils.MetricsUtils;
import io.arbitrix.core.common.monitor.rest.MonitorableResponse;
import io.arbitrix.core.common.util.JacksonUtil;

import java.time.Duration;
import java.util.Objects;

/**
 * @author mcx
 * @date 2023/10/11
 * @description
 */
@Log4j2
public class RestMonitor {
    public static void recordReceiveDelay(String apiName, MonitorableResponse response, Long receiveTime) {
        Long serverTime = StringUtils.isEmpty(response.serverTime()) ? null : Long.parseLong(response.serverTime());
        if (Objects.isNull(serverTime)) {
            log.debug("serverTime is null, exchange:{}, apiName:{}, response:{}", response.exchangeName(), apiName, JacksonUtil.toJsonStr(response));
            return;
        }
        long duration = receiveTime - serverTime;
        if (duration > 100) {
            log.warn("receive delay is too long, exchange:{}, apiName:{}, duration:{}, response:{}", response.exchangeName(), apiName, duration,response);
        }
        MetricsUtils.recordTimeDefaultPercentiles("rest_api_receive_delay", "rest_api_receive_delay",
                Duration.ofMillis(duration),
                "exchange", response.exchangeName(),
                "api", apiName);
    }

    public static void recordSendingDuration(String apiName, MonitorableResponse response, Long startTime) {
        Long serverTime = StringUtils.isEmpty(response.serverTime()) ? null : Long.parseLong(response.serverTime());
        if (Objects.isNull(serverTime)) {
            log.debug("serverTime is null, exchange:{}, apiName:{}, response:{}", response.exchangeName(), apiName, JacksonUtil.toJsonStr(response));
            return;
        }
        long duration = serverTime - startTime;
        if (duration > 100) {
            log.warn("sending duration is too long, exchange:{}, apiName:{}, duration:{}, response:{}", response.exchangeName(), apiName, duration,response);
        }
        MetricsUtils.recordTimeDefaultPercentiles("rest_api_sending_duration", "rest_api_sending_duration",
                Duration.ofMillis(duration),
                "exchange", response.exchangeName(),
                "api", apiName);
    }

    public static void recordDuration(String apiName, MonitorableResponse response, Duration duration) {
        long cost = duration.toMillis();
        if (cost > 100) {
            log.warn("api duration is too long, exchange:{}, apiName:{}, duration:{}, response:{}", response.exchangeName(), apiName, cost,response);
        }
        MetricsUtils.recordTimeDefaultPercentiles("rest_api_duration", "rest_api_duration",
                duration,
                "exchange", response.exchangeName(),
                "api", apiName);
    }
}
