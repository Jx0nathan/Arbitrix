package io.arbitrix.core.common.monitor;

import lombok.extern.log4j.Log4j2;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import io.arbitrix.core.common.monitor.utils.MetricsUtils;
import io.arbitrix.core.facade.MarketFacade;
import io.arbitrix.core.utils.ExchangeMarketOpenUtilV2;
import io.arbitrix.core.utils.SystemClock;
import io.arbitrix.core.utils.executor.NamedThreadFactory;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;


@Log4j2
// 使用component注解，是因为可以比声明在MonitorConfiguration中的bean更早执行
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ServerTimeMonitor {
    private final ScheduledExecutorService SERVER_TIME_SCHEDULER = new ScheduledThreadPoolExecutor(1,
            new NamedThreadFactory("ServerTimeMonitor", true));
    private final MarketFacade marketFacade;
    private final ExchangeMarketOpenUtilV2 exchangeMarketOpenUtilV2;

    public ServerTimeMonitor(MarketFacade marketFacade, ExchangeMarketOpenUtilV2 exchangeMarketOpenUtilV2) {
        this.marketFacade = marketFacade;
        this.exchangeMarketOpenUtilV2 = exchangeMarketOpenUtilV2;
    }
    @PostConstruct
    public void startServerTimeMonitor() {
        // 不单单是为了监控，还包括了预热对应exchange的httpclient,以及对httpclient中的链接进行保活
        // 先进行一次rest调用，预热httpclient
//        this.recordServerTime();
//        SERVER_TIME_SCHEDULER.scheduleAtFixedRate(this::recordServerTime, 0, 1, java.util.concurrent.TimeUnit.SECONDS);
    }

    private void recordServerTime() {
        long localServerTime = SystemClock.now();
        long remoteServerTime = this.marketFacade.getServerTime(this.exchangeMarketOpenUtilV2.getExchange().name());
        long diff = localServerTime - remoteServerTime;
        MetricsUtils.recordTimeDefaultPercentiles("server_time_diff", "server_time_diff", Duration.ofMillis(diff));
        //log.info("localServerTime:{},remoteServerTime:{},diff:{}", localServerTime, remoteServerTime, diff);
    }
}
