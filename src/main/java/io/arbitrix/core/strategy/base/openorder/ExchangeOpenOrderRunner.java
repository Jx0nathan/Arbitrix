package io.arbitrix.core.strategy.base.openorder;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import io.arbitrix.core.common.domain.Order;
import io.arbitrix.core.common.enums.ExchangeNameEnum;
import io.arbitrix.core.utils.ExchangeMarketOpenUtilV2;
import io.arbitrix.core.utils.executor.NamedThreadFactory;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 检测用户的挂单是否需要取消，一个兜底的策略
 *
 * @author jonathan.ji
 */
@Log4j2
public abstract class ExchangeOpenOrderRunner {
    private final ScheduledExecutorService SCHEDULER = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("OpenOrderRunner", true));

    protected final ExchangeNameEnum exchange;
    protected final List<String> symbolPairs;
    protected final ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil;

    public ExchangeOpenOrderRunner(ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil, ExchangeNameEnum exchange) {
        this.exchangeMarketOpenUtil = exchangeMarketOpenUtil;
        this.exchange = exchange;
        this.symbolPairs = exchangeMarketOpenUtil.getSymbolPairs(exchange);
    }

    public void start(Function<List<Order>, List<Order>> checkOrderCanCancelFunction) {
        if (!CollectionUtils.isEmpty(symbolPairs)) {
            SCHEDULER.scheduleAtFixedRate(() -> {
                try {
                    List<Order> orderList = getOpenOrders();
                    List<Order> cancelOrderList = checkOrderCanCancelFunction.apply(orderList);
                    cancelOpenOrders(cancelOrderList);
                } catch (Exception e) {
                    log.error("ExchangeOpenOrderRunner.cancelOpenOrders error,exchange:{}", exchange, e);
                }
            }, 3000, 500, TimeUnit.MILLISECONDS);
        } else {
            log.info("ExchangeOpenOrderRunner.start symbolPairs is empty,exchange:{}", exchange);
        }
    }

    /**
     * 获取对应交易所的未成交的订单
     *
     * @return 未成交的订单
     */
    abstract protected List<Order> getOpenOrders();

    public abstract long getExpireTime();

    /**
     * 取消订单
     *
     * @param cancelOrderList 可以取消的订单
     */
    abstract protected void cancelOpenOrders(List<Order> cancelOrderList);

}
