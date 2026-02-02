package io.arbitrix.core.strategy.base.action;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.arbitrix.core.common.domain.Order;
import io.arbitrix.core.common.enums.ExchangeNameEnum;
import io.arbitrix.core.common.orderbook.OwnOrderBook;
import io.arbitrix.core.strategy.base.openorder.BitgetOpenOrderRunner;
import io.arbitrix.core.strategy.base.openorder.BybitOpenOrderRunner;
import io.arbitrix.core.strategy.base.openorder.ExchangeOpenOrderRunner;
import io.arbitrix.core.strategy.base.openorder.OkxOpenOrderRunner;
import io.arbitrix.core.utils.ExchangeMarketOpenUtilV2;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author jonathan.ji
 */
@AllArgsConstructor
@Slf4j
public abstract class AbstractOrderTradeDataManager {
    private final ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil;
    private final ExchangeOpenOrderRunner binanceOpenOrderRunner;
    private final OkxOpenOrderRunner okxOpenOrderRunner;
    private final BitgetOpenOrderRunner bitgetOpenOrderRunner;
    private final BybitOpenOrderRunner bybitOpenOrderRunner;

    public void startOpenOrderRunner(Function<List<Order>, List<Order>> cancelOrderOrCleanPoolProcessor) {
        // note:一个进程跑多个策略是有问题的
        if (exchangeMarketOpenUtil.exchangeCanStart(ExchangeNameEnum.BINANCE)) {
            binanceOpenOrderRunner.start(cancelOrderOrCleanPoolProcessor);
        }
        if (exchangeMarketOpenUtil.exchangeCanStart(ExchangeNameEnum.OKX)) {
            okxOpenOrderRunner.start(cancelOrderOrCleanPoolProcessor);
        }
        if (exchangeMarketOpenUtil.exchangeCanStart(ExchangeNameEnum.BITGET)) {
            bitgetOpenOrderRunner.start(cancelOrderOrCleanPoolProcessor);
        }
        if (exchangeMarketOpenUtil.exchangeCanStart(ExchangeNameEnum.BYBIT)) {
            bybitOpenOrderRunner.start(cancelOrderOrCleanPoolProcessor);
        }
    }
}
