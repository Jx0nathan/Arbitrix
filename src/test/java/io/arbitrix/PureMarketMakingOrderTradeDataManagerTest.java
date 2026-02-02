package io.arbitrix;

import lombok.Setter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import io.arbitrix.core.common.domain.Order;
import io.arbitrix.core.common.enums.*;
import io.arbitrix.core.common.event.OrderTradeUpdateEvent;
import io.arbitrix.core.integration.binance.rest.BinanceClient;
import io.arbitrix.core.integration.bitget.rest.BitgetRestClient;
import io.arbitrix.core.integration.bybit.rest.BybitRestClient;
import io.arbitrix.core.integration.okx.rest.OkxOpenOrderClient;
import io.arbitrix.core.strategy.base.openorder.BinanceOpenOrderRunner;
import io.arbitrix.core.strategy.base.openorder.BitgetOpenOrderRunner;
import io.arbitrix.core.strategy.base.openorder.BybitOpenOrderRunner;
import io.arbitrix.core.strategy.base.openorder.OkxOpenOrderRunner;
import io.arbitrix.core.strategy.pure_market_making.data.PureMarketMakingSpotOrderTradeDataManager;
import io.arbitrix.core.utils.ExchangeMarketOpenUtilV2;
import io.arbitrix.core.utils.OrderTradeUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Setter
public class PureMarketMakingOrderTradeDataManagerTest {
    private final List<Order> orderList = new ArrayList<>();
    private PureMarketMakingSpotOrderTradeDataManager pureMarketMakingSpotOrderTradeDataManager;

    private ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil;
    private BinanceOpenOrderRunner binanceOpenOrderRunner;
    private OkxOpenOrderRunner okxOpenOrderRunner;
    private BitgetOpenOrderRunner bitgetOpenOrderRunner;
    private BybitOpenOrderRunner bybitOpenOrderRunner;


    @BeforeEach
    public void setUp() {
        pureMarketMakingSpotOrderTradeDataManager = new PureMarketMakingSpotOrderTradeDataManager(binanceOpenOrderRunner, okxOpenOrderRunner, bitgetOpenOrderRunner, bybitOpenOrderRunner, exchangeMarketOpenUtil);

        // 2023-08-24 10:30:21
        Order order1 = Order.builder().symbol("ETHUSDT").orderId("353238").price("958.82600000").origQty("0.00290000").status(OrderStatus.NEW)
                .clientOrderId("88e52e69-a8df-4076-88b6-4ddda44acfac").timeInForce(TimeInForce.GTC).type(OrderType.LIMIT).side(OrderSide.BUY)
                .time(1692844221623L).updateTime(1692844221623L).working(true).build();
        orderList.add(order1);

        // 2023-08-24 11:17:01
        Order order2 = Order.builder().symbol("ETHUSDT").orderId("353241").price("958.81600000").origQty("0.00280000").status(OrderStatus.NEW)
                .clientOrderId("c4dd96c8-0bb5-418e-8a60-1d2376c46da0").timeInForce(TimeInForce.GTC).type(OrderType.LIMIT).side(OrderSide.BUY)
                .time(1692847021898L).updateTime(1692847021898L).working(true).build();
        orderList.add(order2);

        // 2023-08-24 14:19:33
        Order order3 = Order.builder().symbol("ETHUSDT").orderId("353256").price("960.72600000").origQty("0.01").status(OrderStatus.NEW)
                .clientOrderId("a561c0cb-1bf3-431e-bc97-f207779048b0").timeInForce(TimeInForce.GTC).type(OrderType.LIMIT).side(OrderSide.SELL)
                .time(1692857973416L).updateTime(1692857973416L).working(true).build();
        orderList.add(order3);

        // 2023-08-24 11:32:59
        Order order4 = Order.builder().symbol("ETHUSDT").orderId("353243").price("961.72600000").origQty("0.02").status(OrderStatus.NEW)
                .clientOrderId("7e083f47-8817-4d4f-94ae-e2eed23cbc38").timeInForce(TimeInForce.GTC).type(OrderType.LIMIT).side(OrderSide.SELL)
                .time(1692847979995L).updateTime(1692847979995L).working(true).build();
        orderList.add(order4);
    }

    /**
     * 用户未取消的订单，只取最新的订单存放在缓存池中
     */
    @Test
    public void updateOrderTradePoolTest() {
        Map<OrderSide, List<Order>> groupedMap = orderList.stream().collect(Collectors.groupingBy(Order::getSide));
        pureMarketMakingSpotOrderTradeDataManager.updateOrderTradePool(ExchangeNameEnum.BINANCE.name(), groupedMap.get(OrderSide.BUY));
        pureMarketMakingSpotOrderTradeDataManager.updateOrderTradePool(ExchangeNameEnum.BINANCE.name(), groupedMap.get(OrderSide.SELL));

        Map<String, OrderTradeUpdateEvent> orderTradeUpdateEventMap = pureMarketMakingSpotOrderTradeDataManager.getOrderTradePool();
        String cacheKey1 = OrderTradeUtil.buildOrderTradeKey(ExchangeNameEnum.BINANCE.name(), "ETHUSDT", OrderSide.BUY);
        OrderTradeUpdateEvent orderTradeUpdateEvent1 = orderTradeUpdateEventMap.get(cacheKey1);
        Assertions.assertEquals(orderTradeUpdateEvent1.getPrice(), "958.81600000");
        Assertions.assertEquals(orderTradeUpdateEvent1.getOriginalQuantity(), "0.00280000");
        Assertions.assertEquals(orderTradeUpdateEvent1.getEventTime().longValue(), 1692847021898L);

        String cacheKey2 = OrderTradeUtil.buildOrderTradeKey(ExchangeNameEnum.BINANCE.name(), "ETHUSDT", OrderSide.SELL);
        OrderTradeUpdateEvent orderTradeUpdateEvent2 = orderTradeUpdateEventMap.get(cacheKey2);
        Assertions.assertEquals(orderTradeUpdateEvent2.getPrice(), "960.72600000");
        Assertions.assertEquals(orderTradeUpdateEvent2.getOriginalQuantity(), "0.01");
        Assertions.assertEquals(orderTradeUpdateEvent2.getEventTime().longValue(), 1692857973416L);
    }

    /**
     * 如果是买单，初始订单的价格为0，数量为0
     * 如果是卖单，初始订单的价格为最大值，数量为0
     */
    @Test
    public void buildInitOrderTest() {
        pureMarketMakingSpotOrderTradeDataManager.buildInitOrder(ExchangeNameEnum.BINANCE.name(), "BTCUSDT", OrderSide.BUY);
        Map<String, OrderTradeUpdateEvent> orderTradeUpdateEventMap = pureMarketMakingSpotOrderTradeDataManager.getOrderTradePool();
        String symbolKey = OrderTradeUtil.buildOrderTradeKey(ExchangeNameEnum.BINANCE.name(), "BTCUSDT", OrderSide.BUY);
        OrderTradeUpdateEvent orderTradeUpdateEvent = orderTradeUpdateEventMap.get(symbolKey);

        Assertions.assertEquals(orderTradeUpdateEvent.getPrice(), BigDecimal.ZERO.toString());
        Assertions.assertEquals(orderTradeUpdateEvent.getOriginalQuantity(), BigDecimal.ZERO.toString());

        pureMarketMakingSpotOrderTradeDataManager.buildInitOrder(ExchangeNameEnum.BINANCE.name(), "btcbnb", OrderSide.SELL);
        Map<String, OrderTradeUpdateEvent> orderTradeUpdateEventMap2 = pureMarketMakingSpotOrderTradeDataManager.getOrderTradePool();
        String symbolKey2 = OrderTradeUtil.buildOrderTradeKey(ExchangeNameEnum.BINANCE.name(), "btcbnb", OrderSide.SELL);
        OrderTradeUpdateEvent orderTradeUpdateEvent2 = orderTradeUpdateEventMap2.get(symbolKey2);

        Assertions.assertEquals(orderTradeUpdateEvent2.getPrice(), BigDecimal.valueOf(Long.MAX_VALUE).toString());
        Assertions.assertEquals(orderTradeUpdateEvent2.getOriginalQuantity(), BigDecimal.ZERO.toString());
    }
}
