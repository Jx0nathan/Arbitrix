package io.arbitrix.core.integration.bybit.ping;

import lombok.extern.log4j.Log4j2;
import io.arbitrix.core.integration.bybit.wss.dto.req.WsBaseReq;
import io.arbitrix.core.integration.bybit.wss.BybitWebSocketConnection;
import io.arbitrix.core.utils.executor.NamedThreadFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author mcx
 * @date 2023/9/25
 * @description
 */
@Log4j2
public class BybitWSPinger {
    private final ScheduledExecutorService PING_SCHEDULER = new ScheduledThreadPoolExecutor(1,
            new NamedThreadFactory("BybitWSPinger-ping", true));

    private final BybitWebSocketConnection bybitWebSocketConnection;

    public BybitWSPinger(BybitWebSocketConnection bybitWebSocketConnection) {
        this.bybitWebSocketConnection = bybitWebSocketConnection;
    }

    public void start() {
        PING_SCHEDULER.scheduleAtFixedRate(() -> {
            log.info("BybitWSPinger.start");
            ping();
        }, 10, 20, TimeUnit.SECONDS);
    }
    public void ping(){
        bybitWebSocketConnection.sendMessage(WsBaseReq.ping());
    }
}
