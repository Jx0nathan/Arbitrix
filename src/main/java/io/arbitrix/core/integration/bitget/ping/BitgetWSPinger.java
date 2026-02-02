package io.arbitrix.core.integration.bitget.ping;

import lombok.extern.log4j.Log4j2;
import io.arbitrix.core.integration.bitget.wss.BitGetWebSocketConnection;
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
public class BitgetWSPinger {
    private final ScheduledExecutorService PING_SCHEDULER = new ScheduledThreadPoolExecutor(1,
            new NamedThreadFactory("BitgetWSPinger-ping", true));

    private final BitGetWebSocketConnection bitGetWebSocketConnection;

    public BitgetWSPinger(BitGetWebSocketConnection bitGetWebSocketConnection) {
        this.bitGetWebSocketConnection = bitGetWebSocketConnection;
    }

    public void start() {
        PING_SCHEDULER.scheduleAtFixedRate(() -> {
            log.info("BitgetWSPinger.start");
            ping();
        }, 25, 25, TimeUnit.SECONDS);
    }
    public void ping(){
        bitGetWebSocketConnection.sendMessage("ping");
    }
}
