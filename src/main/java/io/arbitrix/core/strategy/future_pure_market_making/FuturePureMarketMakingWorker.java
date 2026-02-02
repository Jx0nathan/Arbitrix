package io.arbitrix.core.strategy.future_pure_market_making;

import io.arbitrix.core.common.util.TrackingUtils;
import io.arbitrix.core.common.util.EnvUtil;
import lombok.extern.log4j.Log4j2;
import net.openhft.affinity.AffinityLock;
import org.springframework.stereotype.Component;
import io.arbitrix.core.common.domain.FutureOrderExecutionContext;
import io.arbitrix.core.common.enums.OrderSide;
import io.arbitrix.core.common.event.BookTickerEvent;
import io.arbitrix.core.common.event.OrderTradeUpdateEvent;
import io.arbitrix.core.strategy.base.action.BookTickerEventListener;
import io.arbitrix.core.strategy.base.action.FutureStrategy;
import io.arbitrix.core.strategy.base.condition.ExecuteStrategyConditional;
import io.arbitrix.core.strategy.future_pure_market_making.data.FuturePureMarketMakingOrderTradeDataManager;
import io.arbitrix.core.strategy.future_pure_market_making.strategy.FuturePureMarketMakingStrategy;
import io.arbitrix.core.utils.SystemClock;
import io.arbitrix.core.utils.executor.*;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import static io.arbitrix.core.strategy.base.enums.FutureExecuteStrategyEnum.FUTURE_PURE_MARKET_MAKING_STRATEGY;


@Log4j2
@Component
@ExecuteStrategyConditional(executeStrategyName = "future_pure_market_making")
public class FuturePureMarketMakingWorker implements BookTickerEventListener {
    private static final String TRACE_ID_FORMAT = "%s_%s_%s_%s_%s";
    private final FuturePureMarketMakingOrderTradeDataManager pureMarketMakingFutureOrderTradeDataManager;
    private final String futureExecuteStrategyName;
    private final FutureStrategy futureStrategy;
    private final ThreadPoolExecutor FUTURE_OPEN_EXECUTOR = FutureOpenExecutor.getInstance();
    private final ThreadPoolExecutor FUTURE_CLOSE_EXECUTOR = FutureCloseExecutor.getInstance();
    private final ThreadPoolExecutor MARKER_MAKER_EXECUTOR = MarkerMakerExecutor.getInstance();

    public FuturePureMarketMakingWorker(FuturePureMarketMakingOrderTradeDataManager pureMarketMakingSpotOrderTradeDataManager,
                                        FuturePureMarketMakingStrategy futureStrategy) {
        this.pureMarketMakingFutureOrderTradeDataManager = pureMarketMakingSpotOrderTradeDataManager;
        this.futureStrategy = futureStrategy;
        this.futureExecuteStrategyName = EnvUtil.getProperty(FUTURE_PURE_MARKET_MAKING_STRATEGY);
    }

    private String generateTraceId(String exchangeName, BookTickerEvent bookTickerEvent) {
        return String.format(TRACE_ID_FORMAT, exchangeName, bookTickerEvent.getSymbol(), bookTickerEvent.getBidPrice(), bookTickerEvent.getAskPrice(), TrackingUtils.generateUUID());
    }

    /**
     * @param bookTickerEvent 最优挂单信息流
     */
    @Override
    public void onBookTicker(String exchangeName, BookTickerEvent bookTickerEvent) {
        //TODO quantity配置从base coin 变成quote coin还没实现
        TrackingUtils.saveTrace(generateTraceId(exchangeName, bookTickerEvent));
        log.info("FuturePureMarketMakingWorker.onBookTicker.start, bookTickerEvent is {}", bookTickerEvent);
        MARKER_MAKER_EXECUTOR.execute(() -> {
            // 获取订单缓存池
            Map<String, OrderTradeUpdateEvent> orderTradeUpdateEventMap = pureMarketMakingFutureOrderTradeDataManager.getOrderTradePool();
            bookTickerEvent.setArrivalTime(SystemClock.now());

            FUTURE_OPEN_EXECUTOR.execute(() -> {
                try (@SuppressWarnings("unused") final AffinityLock al3 = AffinityLock.acquireLock(3)) {
                    FutureOrderExecutionContext openOrderExecutionContext = FutureOrderExecutionContext.builder()
                            .exchangeName(exchangeName)
                            .futureExecuteStrategyName(futureExecuteStrategyName)
                            .symbol(bookTickerEvent.getSymbol())
                            .orderSide(OrderSide.BUY)
                            .bookTickerEvent(bookTickerEvent)
                            .orderTradeUpdateEventMap(orderTradeUpdateEventMap).build();
                    futureStrategy.execute(openOrderExecutionContext);
                } catch (Exception ex) {
                    log.error("PureMarketMakingFutureWorker.onBookTicker.beginTradeForBuy error ", ex);
                }
            });

            FUTURE_CLOSE_EXECUTOR.execute(() -> {
                try (@SuppressWarnings("unused") final AffinityLock al4 = AffinityLock.acquireLock(4)) {
                    FutureOrderExecutionContext closeOrderOrderContext = FutureOrderExecutionContext.builder()
                            .exchangeName(exchangeName)
                            .futureExecuteStrategyName(futureExecuteStrategyName)
                            .symbol(bookTickerEvent.getSymbol())
                            .orderSide(OrderSide.SELL)
                            .bookTickerEvent(bookTickerEvent)
                            .orderTradeUpdateEventMap(orderTradeUpdateEventMap).build();
                    futureStrategy.execute(closeOrderOrderContext);
                } catch (Exception ex) {
                    log.error("PureMarketMakingFutureWorker.onBookTicker.beginTradeForSell error ", ex);
                }
            });
        });
    }
}
