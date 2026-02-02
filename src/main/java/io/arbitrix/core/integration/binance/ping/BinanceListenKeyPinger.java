package io.arbitrix.core.integration.binance.ping;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import io.arbitrix.core.common.enums.ExchangeNameEnum;
import io.arbitrix.core.utils.ExchangeMarketOpenUtilV2;
import io.arbitrix.core.utils.executor.NamedThreadFactory;

import javax.annotation.PostConstruct;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * ListenKey的有效期延长，官方建议每30分钟发送一个ping
 *
 * @author jonathan.ji
 */
@Component
@AllArgsConstructor
@Log4j2
public class BinanceListenKeyPinger {
    private final ScheduledExecutorService SCHEDULER = new ScheduledThreadPoolExecutor(1,
            new NamedThreadFactory("binanceListenKey-ping", true));

    private final BinanceListenKeyService binanceListenKeyService;
    private final ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil;

    @PostConstruct
    public void start() {
        if (exchangeMarketOpenUtil.exchangeCanStart(ExchangeNameEnum.BINANCE)) {
            SCHEDULER.scheduleAtFixedRate(() -> {
                log.info("BinanceListenKeyPinger.start");
                binanceListenKeyService.extendListenKey();
            }, 1, 28, TimeUnit.MINUTES);
        }
    }
}
