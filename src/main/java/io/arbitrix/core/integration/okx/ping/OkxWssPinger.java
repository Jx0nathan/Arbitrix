package io.arbitrix.core.integration.okx.ping;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import io.arbitrix.core.common.enums.ExchangeNameEnum;
import io.arbitrix.core.integration.okx.wss.OkxPrivateWebsocketClient;
import io.arbitrix.core.integration.okx.wss.OkxPublicWebsocketClient;
import io.arbitrix.core.strategy.base.condition.ExchangeConditional;
import io.arbitrix.core.utils.ExchangeMarketOpenUtilV2;
import io.arbitrix.core.utils.executor.NamedThreadFactory;

import javax.annotation.PostConstruct;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author jonathan.ji
 */
@Component
@AllArgsConstructor
@Log4j2
@ExchangeConditional(exchangeName = "OKX")
public class OkxWssPinger {
    private final OkxPrivateWebsocketClient okxPrivateWebsocketClient;
    private final OkxPublicWebsocketClient okxPublicWebsocketClient;
    private final ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil;

    private final ScheduledExecutorService PRIVATE_SCHEDULER = new ScheduledThreadPoolExecutor(1,
            new NamedThreadFactory("OkxWssPrivatePinger-ping", true));

    private final ScheduledExecutorService PUBLIC_SCHEDULER = new ScheduledThreadPoolExecutor(1,
            new NamedThreadFactory("OkxWssPublicPinger-ping", true));

    @PostConstruct
    public void start() {
        if (exchangeMarketOpenUtil.exchangeCanStart(ExchangeNameEnum.OKX)) {
            PRIVATE_SCHEDULER.scheduleAtFixedRate(() -> {
                log.info("okxPrivateWebsocketClient.start");
                okxPrivateWebsocketClient.ping();
            }, 1, 29, TimeUnit.SECONDS);

            PUBLIC_SCHEDULER.scheduleAtFixedRate(() -> {
                log.info("okxPublicWebsocketClient.start");
                okxPublicWebsocketClient.ping();
            }, 1, 29, TimeUnit.SECONDS);
        }
    }
}
