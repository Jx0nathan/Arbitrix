package io.arbitrix.core.strategy.profit_market_making;

import io.arbitrix.core.common.util.TrackingUtils;
import io.arbitrix.core.common.util.EnvUtil;
import lombok.extern.log4j.Log4j2;
import net.openhft.affinity.AffinityLock;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import io.arbitrix.core.common.domain.SpotOrderExecutionContext;
import io.arbitrix.core.common.enums.OrderLevel;
import io.arbitrix.core.common.enums.OrderSide;
import io.arbitrix.core.common.event.BookTickerEvent;
import io.arbitrix.core.common.orderbook.OwnOrderBook;
import io.arbitrix.core.strategy.base.action.OrderBookDepthEventListener;
import io.arbitrix.core.strategy.base.condition.ExecuteStrategyConditional;
import io.arbitrix.core.strategy.profit_market_making.data.ProfitMarketMakingSpotOrderTradeDataManager;
import io.arbitrix.core.strategy.profit_market_making.order.ProfitMarketMakingSpotStrategy;
import io.arbitrix.core.strategy.profit_market_making.order.ProfitOrderPlaceStrategy;
import io.arbitrix.core.utils.ExchangeMarketOpenUtilV2;
import io.arbitrix.core.utils.SystemClock;
import io.arbitrix.core.utils.executor.MarkerMakerExecutor;
import io.arbitrix.core.utils.executor.PlaceBuyOrderExecutor;
import io.arbitrix.core.utils.executor.PlaceSellOrderExecutor;
import io.arbitrix.core.common.util.JacksonUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import static io.arbitrix.core.strategy.base.enums.ApplicationExecuteStrategyEnum.PROFIT_MARKET_MAKING;
import static io.arbitrix.core.utils.ExchangeMarketOpenUtilV2.SIDE_TYPE;

/**
 * @author jonathan.ji
 */
@Component
@Order(99)
@Log4j2
@ExecuteStrategyConditional(executeStrategyName = "profit_market_making")
public class ProfitMarketMakingSpotWorker implements OrderBookDepthEventListener {
    private final ProfitMarketMakingSpotOrderTradeDataManager profitOrderTradeDataManager;
    private final ProfitMarketMakingSpotStrategy fixedSpreadBasedOrderPlace;
    private final ProfitOrderPlaceStrategy profitOrderPlaceStrategy;
    private final String sideType;

    private final ThreadPoolExecutor PLACE_BUY_ORDER_EXECUTOR = PlaceBuyOrderExecutor.getInstance();
    private final ThreadPoolExecutor PLACE_SELL_ORDER_EXECUTOR = PlaceSellOrderExecutor.getInstance();
    private final ThreadPoolExecutor MARKER_MAKER_EXECUTOR = MarkerMakerExecutor.getInstance();


    public ProfitMarketMakingSpotWorker(ProfitMarketMakingSpotOrderTradeDataManager profitOrderTradeDataManager, ProfitMarketMakingSpotStrategy fixedSpreadBasedOrderPlace, ProfitOrderPlaceStrategy profitOrderPlaceStrategy) {
        this.profitOrderTradeDataManager = profitOrderTradeDataManager;
        this.fixedSpreadBasedOrderPlace = fixedSpreadBasedOrderPlace;
        this.profitOrderPlaceStrategy = profitOrderPlaceStrategy;
        sideType = EnvUtil.getProperty(SIDE_TYPE);
    }

    @Override
    public String getCurrentStrategyName() {
        return PROFIT_MARKET_MAKING.getStrategyName();
    }

    /**
     * 监听深度信息流，生成买卖单
     */
    @Override
    public void onDepthOrderBook(String exchangeName, List<BookTickerEvent> eventList) {
        TrackingUtils.saveTrace(generateTraceId(exchangeName, eventList.get(0).getSymbol(), eventList.get(0).getBidPrice(), eventList.get(0).getAskPrice()));
        log.debug("ProfitMarketMakingSpotWorker.onBookTicker.start, bookTickerEventList is {}", JacksonUtil.toJsonStr(eventList));

        // 获取用户设定的策略，需要在哪几档放置订单
        List<OrderLevel> orderLevelList = profitOrderPlaceStrategy.getOrderLevelList();
        for (BookTickerEvent event : eventList) {
            OrderLevel orderEventLevel = event.getOrderLevel();
            boolean pickOrderLevel = orderLevelList.contains(orderEventLevel);
            if (pickOrderLevel) {
                MARKER_MAKER_EXECUTOR.execute(() -> {
                    // 获取订单缓存池
                    Map<String, OwnOrderBook> ownOrderBook = profitOrderTradeDataManager.getOrderTradePool();
                    event.setArrivalTime(SystemClock.now());

                    // 生成买单
                    PLACE_BUY_ORDER_EXECUTOR.execute(() -> {
                        boolean result = ExchangeMarketOpenUtilV2.checkSideType(sideType, "buy");
                        if (!result) {
                            return;
                        }
                        try (@SuppressWarnings("unused") final AffinityLock al3 = AffinityLock.acquireLock(3)) {
                            SpotOrderExecutionContext buyOrderContext = SpotOrderExecutionContext.builder()
                                    .orderLevel(orderEventLevel.getLevel())
                                    .exchangeName(exchangeName)
                                    .symbol(event.getSymbol())
                                    .orderSide(OrderSide.BUY)
                                    .bookTickerEvent(event)
                                    .ownOrderBook(ownOrderBook).build();
                            fixedSpreadBasedOrderPlace.execute(buyOrderContext);
                        } catch (Exception ex) {
                            log.error("ProfitMarketMakingSpotWorker.onBookTicker.beginTradeForBuy error ", ex);
                        }
                    });

                    // 生成卖单
                    PLACE_SELL_ORDER_EXECUTOR.execute(() -> {
                        boolean result = ExchangeMarketOpenUtilV2.checkSideType(sideType, "sell");
                        if (!result) {
                            return;
                        }
                        try (@SuppressWarnings("unused") final AffinityLock al4 = AffinityLock.acquireLock(4)) {
                            SpotOrderExecutionContext sellOrderContext = SpotOrderExecutionContext.builder()
                                    .orderLevel(orderEventLevel.getLevel())
                                    .exchangeName(exchangeName)
                                    .symbol(event.getSymbol())
                                    .orderSide(OrderSide.SELL)
                                    .bookTickerEvent(event)
                                    .ownOrderBook(ownOrderBook).build();
                            fixedSpreadBasedOrderPlace.execute(sellOrderContext);
                        } catch (Exception ex) {
                            log.error("ProfitMarketMakingSpotWorker.onBookTicker.beginTradeForSell error ", ex);
                        }
                    });
                });
            }
        }
    }
}
