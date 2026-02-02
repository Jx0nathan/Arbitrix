package io.arbitrix.core.common.monitor;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import io.arbitrix.core.common.monitor.utils.MetricsUtils;
import io.arbitrix.core.common.enums.WSStreamType;
import io.arbitrix.core.common.monitor.event.MonitorableEvent;
import io.arbitrix.core.common.util.JacksonUtil;

import java.time.Duration;
import java.util.Objects;

/**
 * @author mcx
 * @date 2023/10/11
 * @description
 */
@Log4j2
public class WSSMonitor {
    public static final String TOPIC_BOOK_TICKER = "bookTicker";
    public static final String TOPIC_ORDER_TRADE = "ordersTrade";
    public static final String TOPIC_WALLET = "wallet";

    public static final String TOPIC_DEPTH_N = "depth_%s";
    public static final String TOPIC_DEPTH_1 = "depth_1";
    public static final String TOPIC_DEPTH_5 = "depth_5";


    public static void recordWSSReceiveMessage(String exchange, String topic) {
        MetricsUtils.count("wss_receive_message", "wss receive message",
                "exchange", exchange,
                "topic", topic);
    }

    public static void receiveBookTickerEvent(String exchange) {
        recordWSSReceiveMessage(exchange, TOPIC_BOOK_TICKER);
    }

    public static void receiveOrderTradeEvent(String exchange) {
        recordWSSReceiveMessage(exchange, TOPIC_ORDER_TRADE);
    }

    public static void receiveWalletEvent(String exchange) {
        recordWSSReceiveMessage(exchange, TOPIC_WALLET);
    }

    public static void receiveDepth1Event(String exchange) {
        recordWSSReceiveMessage(exchange, TOPIC_DEPTH_1);
    }

    public static void receiveDepth5Event(String exchange) {
        recordWSSReceiveMessage(exchange, TOPIC_DEPTH_5);
    }


    public static void recordDelay(String exchange, WSStreamType wsStreamType, MonitorableEvent event, long receiveTime) {
        Long eventTime = StringUtils.isEmpty(event.eventTime()) ? null : Long.parseLong(event.eventTime());
        if (Objects.isNull(eventTime)) {
            log.debug("eventTime is null, event: {}", JacksonUtil.toJsonStr(event));
            return;
        }
        MetricsUtils.recordTimeDefaultPercentiles("wss_delay", "wss delay",
                Duration.ofMillis(receiveTime - eventTime),
                "exchange", exchange,
                "stream_type", wsStreamType.name(),
                "topic", event.topic());
    }
}
