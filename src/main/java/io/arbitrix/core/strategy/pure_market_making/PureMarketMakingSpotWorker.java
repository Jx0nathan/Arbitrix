package io.arbitrix.core.strategy.pure_market_making;

import io.arbitrix.core.common.util.TrackingUtils;
import io.arbitrix.core.common.util.EnvUtil;
import lombok.extern.log4j.Log4j2;
import net.openhft.affinity.AffinityLock;
import org.springframework.stereotype.Component;
import io.arbitrix.core.common.domain.SpotOrderExecutionContext;
import io.arbitrix.core.common.enums.OrderSide;
import io.arbitrix.core.common.event.BookTickerEvent;
import io.arbitrix.core.common.event.OrderTradeUpdateEvent;
import io.arbitrix.core.strategy.base.action.BookTickerEventListener;
import io.arbitrix.core.strategy.base.condition.ExecuteStrategyConditional;
import io.arbitrix.core.strategy.base.action.SpotStrategy;
import io.arbitrix.core.strategy.pure_market_making.data.PureMarketMakingSpotOrderTradeDataManager;
import io.arbitrix.core.strategy.pure_market_making.order.PureMarketMakingSpotStrategy;
import io.arbitrix.core.utils.ExchangeMarketOpenUtilV2;
import io.arbitrix.core.utils.SystemClock;
import io.arbitrix.core.utils.executor.MarkerMakerExecutor;
import io.arbitrix.core.utils.executor.PlaceBuyOrderExecutor;
import io.arbitrix.core.utils.executor.PlaceSellOrderExecutor;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import static io.arbitrix.core.utils.ExchangeMarketOpenUtilV2.EXCHANGE;
import static io.arbitrix.core.utils.ExchangeMarketOpenUtilV2.SIDE_TYPE;

/**
 * @author jonathan.ji
 */
@Component
@Log4j2
@ExecuteStrategyConditional(executeStrategyName = "pure_market_making")
public class PureMarketMakingSpotWorker implements BookTickerEventListener {
    private static final String TRACE_ID_FORMAT = "%s_%s_%s_%s_%s";
    private final String sideType;

    private final PureMarketMakingSpotOrderTradeDataManager orderTradeDataManager;

    private final SpotStrategy pureMarketMakingSpotStrategy;

    private final ThreadPoolExecutor PLACE_BUY_ORDER_EXECUTOR = PlaceBuyOrderExecutor.getInstance();
    private final ThreadPoolExecutor PLACE_SELL_ORDER_EXECUTOR = PlaceSellOrderExecutor.getInstance();
    private final ThreadPoolExecutor MARKER_MAKER_EXECUTOR = MarkerMakerExecutor.getInstance();


    public PureMarketMakingSpotWorker(PureMarketMakingSpotOrderTradeDataManager orderTradeDataManager, PureMarketMakingSpotStrategy boostVolumeStrategyImpl) {
        this.orderTradeDataManager = orderTradeDataManager;
        this.pureMarketMakingSpotStrategy = boostVolumeStrategyImpl;
        sideType = EnvUtil.getProperty(SIDE_TYPE);
    }

    private String generateTraceId(String exchangeName, BookTickerEvent bookTickerEvent) {
        return String.format(TRACE_ID_FORMAT, exchangeName, bookTickerEvent.getSymbol(), bookTickerEvent.getBidPrice(), bookTickerEvent.getAskPrice(), TrackingUtils.generateUUID());
    }

    /**
     * 依靠最优挂单信息流，生成买单和卖单
     * 注意：不能自己生成买卖单，这样会导致有订单没被取消
     *
     * @param bookTickerEvent 最优挂单信息流
     */
    @Override
    public void onBookTicker(String exchangeName, BookTickerEvent bookTickerEvent) {
        TrackingUtils.saveTrace(generateTraceId(exchangeName, bookTickerEvent));
        log.info("PureMarketMakingSpotWorker.onBookTicker star, bookTickerEvent is {}", bookTickerEvent);
        MARKER_MAKER_EXECUTOR.execute(() -> {
            // 获取订单缓存池
            Map<String, OrderTradeUpdateEvent> orderTradeUpdateEventMap = orderTradeDataManager.getOrderTradePool();
            bookTickerEvent.setArrivalTime(SystemClock.now());

            // 生成买单
            PLACE_BUY_ORDER_EXECUTOR.execute(() -> {
                boolean result = ExchangeMarketOpenUtilV2.checkSideType(sideType, "buy");
                if (result) {
                    try (@SuppressWarnings("unused") final AffinityLock al3 = AffinityLock.acquireLock(3)) {
                        SpotOrderExecutionContext buyOrderContext = SpotOrderExecutionContext.builder()
                                .exchangeName(exchangeName)
                                .symbol(bookTickerEvent.getSymbol())
                                .orderSide(OrderSide.BUY)
                                .bookTickerEvent(bookTickerEvent)
                                .orderTradeUpdateEventMap(orderTradeUpdateEventMap).build();
                        pureMarketMakingSpotStrategy.execute(buyOrderContext);
                    } catch (Exception ex) {
                        log.error("PureMarketMakingSpotWorker.onBookTicker.beginTradeForBuy error ", ex);
                    }
                }
            });

            // 生成卖单
            PLACE_SELL_ORDER_EXECUTOR.execute(() -> {
                boolean result = ExchangeMarketOpenUtilV2.checkSideType(sideType, "sell");
                if (result) {
                    try (@SuppressWarnings("unused") final AffinityLock al4 = AffinityLock.acquireLock(4)) {
                        SpotOrderExecutionContext sellOrderContext = SpotOrderExecutionContext.builder()
                                .exchangeName(exchangeName)
                                .symbol(bookTickerEvent.getSymbol())
                                .orderSide(OrderSide.SELL)
                                .bookTickerEvent(bookTickerEvent)
                                .orderTradeUpdateEventMap(orderTradeUpdateEventMap).build();
                        pureMarketMakingSpotStrategy.execute(sellOrderContext);
                    } catch (Exception ex) {
                        log.error("PureMarketMakingSpotWorker.onBookTicker.beginTradeForSell error ", ex);
                    }
                }
            });
        });
    }
}
