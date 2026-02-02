package io.arbitrix.profit_market_making;

import org.junit.Ignore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import io.arbitrix.core.common.domain.ExchangeOrder;
import io.arbitrix.core.common.domain.SpotOrderExecutionContext;
import io.arbitrix.core.common.enums.ExchangeNameEnum;
import io.arbitrix.core.common.enums.ExecutionType;
import io.arbitrix.core.common.enums.OrderLevel;
import io.arbitrix.core.common.enums.OrderSide;
import io.arbitrix.core.common.event.BookTickerEvent;
import io.arbitrix.core.common.event.OrderTradeUpdateEvent;
import io.arbitrix.core.common.orderbook.OwnOrderBook;
import io.arbitrix.core.strategy.profit_market_making.data.ProfitMarketMakingSpotOrderTradeDataManager;
import io.arbitrix.core.strategy.profit_market_making.order.OrderBookDepthDistribution;
import io.arbitrix.core.utils.OrderTradeUtil;
import io.arbitrix.core.utils.SystemClock;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 针对EnvUtil类不是很好mock，不浪费太多时间，测试前我先在源码里面临时修改了代码：
 * 修改后： this.exchangeNameEnum = ExchangeNameEnum.BYBIT
 */
@Ignore
public class ProfitPureMarketMakingOrderTradeDataManagerTest {

    private ProfitMarketMakingSpotOrderTradeDataManager profitOrderTradeDataManager;
    @Mock
    private OrderBookDepthDistribution orderBookDepthDistribution;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(orderBookDepthDistribution.getOrderLevelByUUid(anyString())).thenReturn(OrderLevel.FIRST_LEVEL.getLevel());
        profitOrderTradeDataManager = new ProfitMarketMakingSpotOrderTradeDataManager(null,null, null, null, null, null, null);
    }

    @Test
    public void orderTradeUpdateEventTest01() {
        // 预先生成一批订单
        this.putOrderIntoOrderPool();

        // 创建订单完成的事件流
        OrderTradeUpdateEvent event = new OrderTradeUpdateEvent();
        event.setExecutionType(ExecutionType.TRADE);
        event.setSymbol("BTCUSDT");
        event.setSide(OrderSide.BUY);
        event.setOrigClientOrderId("test-1");

        profitOrderTradeDataManager.orderTradeUpdateEvent(ExchangeNameEnum.BYBIT.getValue(), event);

        // 将订单池中的数据删除一个
        Map<String, OwnOrderBook> ownOrderBookMap = profitOrderTradeDataManager.getOrderTradePool();
        Assertions.assertEquals(ownOrderBookMap.size(), 1);
        ownOrderBookMap.forEach((key, orderBook) -> {
            List<OrderTradeUpdateEvent> orderTradeUpdateEventList = orderBook.getOrderTradeUpdateEventList();
            Assertions.assertEquals(orderTradeUpdateEventList.size(), 2);
            Assertions.assertEquals(orderTradeUpdateEventList.get(0).getOrigClientOrderId(), "test-2");
            Assertions.assertEquals(orderTradeUpdateEventList.get(1).getOrigClientOrderId(), "test-3");
        });
    }

    @Test
    public void orderTradeUpdateEventTest02() {
        // 预先生成一批订单
        this.putOrderIntoOrderPool();

        // 创建订单完成的事件流
        OrderTradeUpdateEvent event = new OrderTradeUpdateEvent();
        event.setExecutionType(ExecutionType.TRADE);
        event.setSymbol("BTCUSDT");
        event.setSide(OrderSide.BUY);
        event.setOrigClientOrderId("test-1");

        profitOrderTradeDataManager.orderTradeUpdateEvent(ExchangeNameEnum.BYBIT.getValue(), event);

        // 创建订单完成的事件流
        OrderTradeUpdateEvent event2 = new OrderTradeUpdateEvent();
        event2.setExecutionType(ExecutionType.TRADE);
        event2.setSymbol("BTCUSDT");
        event2.setSide(OrderSide.BUY);
        event2.setOrigClientOrderId("test-2");

        profitOrderTradeDataManager.orderTradeUpdateEvent(ExchangeNameEnum.BYBIT.getValue(), event2);

        // 将订单池中的数据删除一个
        Map<String, OwnOrderBook> ownOrderBookMap = profitOrderTradeDataManager.getOrderTradePool();
        Assertions.assertEquals(ownOrderBookMap.size(), 1);
        ownOrderBookMap.forEach((key, orderBook) -> {
            List<OrderTradeUpdateEvent> orderTradeUpdateEventList = orderBook.getOrderTradeUpdateEventList();
            Assertions.assertEquals(orderTradeUpdateEventList.size(), 1);
            Assertions.assertEquals(orderTradeUpdateEventList.get(0).getOrigClientOrderId(), "test-3");
        });
    }

    @Test
    public void orderTradeUpdateEventTest03() {
        // 预先生成一批订单
        this.putOrderIntoOrderPool();

        // 创建订单完成的事件流
        OrderTradeUpdateEvent event = new OrderTradeUpdateEvent();
        event.setExecutionType(ExecutionType.TRADE);
        event.setSymbol("BTCUSDT");
        event.setSide(OrderSide.BUY);
        event.setOrigClientOrderId("test-1");
        profitOrderTradeDataManager.orderTradeUpdateEvent(ExchangeNameEnum.BYBIT.getValue(), event);

        // 创建订单完成的事件流
        OrderTradeUpdateEvent event2 = new OrderTradeUpdateEvent();
        event2.setExecutionType(ExecutionType.TRADE);
        event2.setSymbol("BTCUSDT");
        event2.setSide(OrderSide.BUY);
        event2.setOrigClientOrderId("test-2");
        profitOrderTradeDataManager.orderTradeUpdateEvent(ExchangeNameEnum.BYBIT.getValue(), event2);

        // 创建订单完成的事件流
        OrderTradeUpdateEvent event3 = new OrderTradeUpdateEvent();
        event3.setExecutionType(ExecutionType.TRADE);
        event3.setSymbol("BTCUSDT");
        event3.setSide(OrderSide.BUY);
        event3.setOrigClientOrderId("test-3");
        profitOrderTradeDataManager.orderTradeUpdateEvent(ExchangeNameEnum.BYBIT.getValue(), event3);

        // 将订单池中的数据删除一个
        Map<String, OwnOrderBook> ownOrderBookMap = profitOrderTradeDataManager.getOrderTradePool();
        Assertions.assertEquals(ownOrderBookMap.size(), 0);
        ownOrderBookMap.forEach((key, orderBook) -> {
            List<OrderTradeUpdateEvent> orderTradeUpdateEventList = orderBook.getOrderTradeUpdateEventList();
            Assertions.assertEquals(orderTradeUpdateEventList.size(), 0);
        });
    }

    @Test
    public void putOrderIntoOrderPoolTest() {
        this.putOrderIntoOrderPool();
        Map<String, OwnOrderBook> ownOrderBookMap = profitOrderTradeDataManager.getOrderTradePool();
        Assertions.assertEquals(ownOrderBookMap.size(), 1);
        ownOrderBookMap.forEach((key, orderBook) -> {
            List<OrderTradeUpdateEvent> orderTradeUpdateEventList = orderBook.getOrderTradeUpdateEventList();
            Assertions.assertEquals(orderTradeUpdateEventList.size(), 3);
            Assertions.assertEquals(orderTradeUpdateEventList.get(0).getOrigClientOrderId(), "test-1");
            Assertions.assertEquals(orderTradeUpdateEventList.get(1).getOrigClientOrderId(), "test-2");
            Assertions.assertEquals(orderTradeUpdateEventList.get(2).getOrigClientOrderId(), "test-3");
        });
    }

    private void putOrderIntoOrderPool() {
        BookTickerEvent bookTickerEvent = new BookTickerEvent();
        bookTickerEvent.setSymbol(OrderSide.BUY.getValue());
        bookTickerEvent.setBidPrice("28000");

        OrderTradeUpdateEvent newOrderTradeBuyEvent = OrderTradeUpdateEvent.createInitOrderTradeUpdateEvent(OrderSide.BUY);
        String anchorPrice = String.valueOf(Integer.MIN_VALUE);

        String cacheKey = OrderTradeUtil.buildLevelOrderTradeKey(ExchangeNameEnum.BYBIT.getValue(), "BTCUSDT", OrderSide.BUY, OrderLevel.FIRST_LEVEL.getLevel());
        OwnOrderBook orderBook = new OwnOrderBook(anchorPrice, SystemClock.now(), List.of(newOrderTradeBuyEvent));

        Map<String, OwnOrderBook> ownOrderBookMap = new HashMap<>();
        ownOrderBookMap.put(cacheKey, orderBook);

        SpotOrderExecutionContext context = SpotOrderExecutionContext.builder()
                .exchangeName(ExchangeNameEnum.BYBIT.getValue())
                .symbol("BTCUSDT")
                .orderSide(OrderSide.BUY)
                .orderLevel(OrderLevel.FIRST_LEVEL.getLevel())
                .bookTickerEvent(bookTickerEvent)
                .ownOrderBook(ownOrderBookMap).build();

        List<ExchangeOrder> sportOrderList = this.generateSportOrderList();
        profitOrderTradeDataManager.putOrderIntoOrderPool(context, sportOrderList);
    }

    private List<ExchangeOrder> generateSportOrderList() {
        ExchangeOrder sportOrder1 = ExchangeOrder.limitMarketBuy("BTCUSDT", "0.1", "28000", "test-1");
        ExchangeOrder sportOrder2 = ExchangeOrder.limitMarketBuy("BTCUSDT", "0.1", "28799", "test-2");
        ExchangeOrder sportOrder3 = ExchangeOrder.limitMarketBuy("BTCUSDT", "0.1", "28798", "test-3");
        return Arrays.asList(sportOrder1, sportOrder2, sportOrder3);
    }
}
