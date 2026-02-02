package io.arbitrix;

import lombok.extern.log4j.Log4j2;
import net.openhft.affinity.AffinityLock;
import org.junit.jupiter.api.Test;
import io.arbitrix.core.common.domain.Order;
import io.arbitrix.core.common.enums.ExchangeNameEnum;
import io.arbitrix.core.common.enums.OrderSide;
import io.arbitrix.core.common.event.OrderTradeUpdateEvent;
import io.arbitrix.core.strategy.pure_market_making.data.PureMarketMakingSpotOrderTradeDataManager;
import io.arbitrix.core.utils.OrderTradeUtil;
import io.arbitrix.core.utils.executor.MarkerMakerExecutor;
import io.arbitrix.core.utils.executor.PlaceBuyOrderExecutor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

@Log4j2
public class DataDelayTest {
    private final ThreadPoolExecutor MARKER_MAKER_EXECUTOR = MarkerMakerExecutor.getInstance();
    private final ThreadPoolExecutor PLACE_BUY_ORDER_EXECUTOR = PlaceBuyOrderExecutor.getInstance();

    private PureMarketMakingSpotOrderTradeDataManager pureMarketMakingSpotOrderTradeDataManager;

    /**
     * 监听订单生成的websocket: 100ms 2 fail 98 success
     * 监听创建订单生成的websocket : 300ms 3 fail 97 success
     */
    @Test
    public void test() {
        BigDecimal basePrice = new BigDecimal("170.00");
        for (int i = 0; i < 100; i++) {
            basePrice = basePrice.add(new BigDecimal("0.01"));
            Order order = new Order();
            order.setPrice(basePrice.toString());

            MARKER_MAKER_EXECUTOR.execute(() -> {
                try (@SuppressWarnings("unused") final AffinityLock al = AffinityLock.acquireLock(3)) {
                    Map<String, OrderTradeUpdateEvent> orderTradeUpdateEventMap = pureMarketMakingSpotOrderTradeDataManager.getOrderTradePool();

                            String cacheKey = OrderTradeUtil.buildOrderTradeKey(ExchangeNameEnum.BINANCE.name(), "ETHUSD", OrderSide.BUY);
                            OrderTradeUpdateEvent item = orderTradeUpdateEventMap.get(cacheKey);
                            if (item == null) {
                                log.error("OrderTradeUpdateEvent is null");
                            } else {
                                log.error("OrderTradeUpdateEvent price is {}", item.getPrice());
                            }
                            PLACE_BUY_ORDER_EXECUTOR.submit(() -> {
                                try (@SuppressWarnings("unused") final AffinityLock al2 = AffinityLock.acquireLock(4)) {
                                    //List<SportOrder> sportOrders = List.of(SportOrder.limitMarketBuy("ETHUSD", "0.1", order.getPrice()));
                                    //binanceWebSocketClient.postOrderBatch(sportOrders, SystemClock.now());
                                }
                            });
                            Thread.sleep(50);
                        } catch (Exception ex) {
                    System.out.printf(("PureMarketMakingSpotWorker onBookTicker error" + ex));
                        }
                    }
            );
        }
    }

}
