package io.arbitrix.core.strategy.profit_market_making.data;

import io.arbitrix.core.common.util.EnvUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import io.arbitrix.core.common.domain.ExchangeOrder;
import io.arbitrix.core.common.domain.Order;
import io.arbitrix.core.common.domain.SpotOrderExecutionContext;
import io.arbitrix.core.common.enums.ExchangeNameEnum;
import io.arbitrix.core.common.enums.ExecutionType;
import io.arbitrix.core.common.enums.OrderLevel;
import io.arbitrix.core.common.enums.OrderSide;
import io.arbitrix.core.common.event.BookTickerEvent;
import io.arbitrix.core.common.event.OrderTradeUpdateEvent;
import io.arbitrix.core.common.orderbook.OwnOrderBook;
import io.arbitrix.core.strategy.base.action.AbstractOrderTradeDataManager;
import io.arbitrix.core.strategy.base.action.OrderTradeUpdateListener;
import io.arbitrix.core.strategy.base.condition.ExecuteStrategyConditional;
import io.arbitrix.core.strategy.base.openorder.*;
import io.arbitrix.core.strategy.profit_market_making.inventory.InventoryTracker;
import io.arbitrix.core.strategy.profit_market_making.order.OrderBookDepthDistribution;
import io.arbitrix.core.strategy.profit_market_making.order.ProfitOrderPlaceStrategy;
import io.arbitrix.core.utils.ExchangeMarketOpenUtilV2;
import io.arbitrix.core.utils.OrderTradeUtil;
import io.arbitrix.core.utils.SystemClock;
import io.arbitrix.core.common.util.JacksonUtil;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.arbitrix.core.utils.ExchangeMarketOpenUtilV2.EXCHANGE;

/**
 * @author jonathan.ji
 */
@Log4j2
@Component("profitOrderTradeDataManager")
@ExecuteStrategyConditional(executeStrategyName = "profit_market_making")
public class ProfitMarketMakingSpotOrderTradeDataManager extends AbstractOrderTradeDataManager implements OrderTradeUpdateListener {
    private volatile Map<String, OwnOrderBook> orderTradePool = new ConcurrentHashMap<>();
    private final ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil;
    private final ExchangeNameEnum exchangeNameEnum;
    private final OrderBookDepthDistribution orderBookDepthDistribution;
    private final ProfitOrderPlaceStrategy profitOrderPlaceStrategy;
    private final InventoryTracker inventoryTracker;

    public ProfitMarketMakingSpotOrderTradeDataManager(BinanceOpenOrderRunner binanceOpenOrderRunner,
                                                       OkxOpenOrderRunner okxOpenOrderRunner,
                                                       BitgetOpenOrderRunner bitgetOpenOrderRunner,
                                                       BybitOpenOrderRunner bybitOpenOrderRunner,
                                                       ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil,
                                                       OrderBookDepthDistribution orderBookDepthDistribution,
                                                       ProfitOrderPlaceStrategy profitOrderPlaceStrategy,
                                                       InventoryTracker inventoryTracker) {
        super(exchangeMarketOpenUtil, binanceOpenOrderRunner, okxOpenOrderRunner, bitgetOpenOrderRunner, bybitOpenOrderRunner);
        this.exchangeMarketOpenUtil = exchangeMarketOpenUtil;
        this.orderBookDepthDistribution = orderBookDepthDistribution;
        this.exchangeNameEnum = ExchangeNameEnum.getExchangeName(EnvUtil.getProperty(EXCHANGE));
        this.inventoryTracker = inventoryTracker;
        this.profitOrderPlaceStrategy = profitOrderPlaceStrategy;
    }

    public Map<String, OwnOrderBook> getOrderTradePool() {
        return orderTradePool;
    }

    /**
     * （1）初始化订单成交事件缓存池 <br>
     * （2）初始化订单池定时清理的逻辑 <br>
     * - 每隔1秒钟清理一次缓存池 (为什么要定时清楚缓存 -> 因为创建的订单的步骤是：入缓存 -> 下单，因此可能下单失败而导致数据被污染，从而影响后续下单触发) <br>
     * - 导致订单池中的数据可能会被清理掉，因此需要定时清理 {@link ExchangeOpenOrderRunner} <br>
     */
    @PostConstruct
    public void initOrderTradePool() {
        if (exchangeNameEnum != null) {
            exchangeMarketOpenUtil.getSymbolPairs(exchangeNameEnum).forEach(symbol -> {
                // 初始化订单池
                this.buildInitOrder(exchangeNameEnum.getValue(), symbol, OrderSide.BUY);
                this.buildInitOrder(exchangeNameEnum.getValue(), symbol, OrderSide.SELL);
            });
        }


        Function<List<Order>, List<Order>> cancelOrderOrCleanPoolProcessor = orders -> {

            // 订单取消的定时任务，遍历所有的挂单，如果这个订单的价格和最优价格不一致，那么就取消这个订单
            List<Order> cancelOrderList = new ArrayList<>();
            for (Order order : orders) {
                int orderLevel = orderBookDepthDistribution.getOrderLevelByUUid(order.getClientOrderId());
                String cacheKey = OrderTradeUtil.buildLevelOrderTradeKey(exchangeNameEnum.getValue(), order.getSymbol(), order.getSide(), orderLevel);
                OwnOrderBook orderBook = orderTradePool.get(cacheKey);
                if (orderBook != null && orderBook.getAnchorPrice() != null) {
                    // 如果价格不相同便取消订单
                    if (isDifferentPrice(orderBook.getAnchorPrice(), order.getPrice())) {
                        cancelOrderList.add(order);
                    } else {
                        log.info("ProfitMarketMakingSpotOrderTradeDataManager.getCancelOrderListBaseOnBestPrice order price is same,exchange:{},orderPrice:{}", exchangeNameEnum.getValue(), order.getPrice());
                    }
                }else {
                    log.info("ProfitMarketMakingSpotOrderTradeDataManager.getCancelOrderListBaseOnBestPrice cacheOrder price is null");
                }
            }

            // 判断订单缓存池是否可以清除
            Map<OrderSide, List<Order>> groupedMap = orders.stream().collect(Collectors.groupingBy(Order::getSide));
            cancelOrderList.addAll(processOrderPollClear(groupedMap.get(OrderSide.BUY)));
            cancelOrderList.addAll(processOrderPollClear(groupedMap.get(OrderSide.SELL)));

            return cancelOrderList;
        };
        super.startOpenOrderRunner(cancelOrderOrCleanPoolProcessor);
    }

    private static boolean isDifferentPrice(String anchorPrice, String price) {
        return new BigDecimal(anchorPrice).compareTo(new BigDecimal(price)) != 0;
    }

    private List<Order> processOrderPollClear(List<Order> orderList) {
        if (CollectionUtils.isEmpty(orderList)) {
            return Collections.emptyList();
        }

        List<Order> sortedList = orderList.stream()
                .sorted(Comparator.comparing(Order::getTime).reversed()).collect(Collectors.toList());
        Order order = sortedList.get(0);

        int orderLevel = orderBookDepthDistribution.getOrderLevelByUUid(order.getClientOrderId());
        String cacheKey = OrderTradeUtil.buildLevelOrderTradeKey(exchangeNameEnum.getValue(), order.getSymbol(), order.getSide(), orderLevel);
        OwnOrderBook orderBook = orderTradePool.get(cacheKey);
        List<Order> needClearOrders = new ArrayList<>();
        if (orderBook != null && isDifferentPrice(orderBook.getAnchorPrice(), order.getPrice())) {
            // 这边只选择清除缓存的原因 是害怕有并发的原因导致的数据污染。订单已经执行，但是还未查询到，如果覆盖，缓存变成脏数据
            OwnOrderBook ownOrderBook = orderTradePool.remove(cacheKey);
            if (Objects.nonNull(ownOrderBook) && !CollectionUtils.isEmpty(ownOrderBook.getOrderTradeUpdateEventList())) {
                log.info("ProfitMarketMakingSpotOrderTradeDataManager.processOrderPollClear cacheKey is {} time {} ownOrderBook is {} ", cacheKey, System.currentTimeMillis(), JacksonUtil.toJsonStr(ownOrderBook));
                for (OrderTradeUpdateEvent orderTradeUpdateEvent : ownOrderBook.getOrderTradeUpdateEventList()) {
                    needClearOrders.add(Order.builder().
                            symbol(orderTradeUpdateEvent.getSymbol()).
                            side(orderTradeUpdateEvent.getSide()).
                            clientOrderId(orderTradeUpdateEvent.getOrigClientOrderId()).build());
                }
            }
        }
        return needClearOrders;
    }

    /**
     * 监听订单成交事件，如果已经成交或者取消，就从缓存池中删除 <br>
     * (1) 如果该档位的无任何订单数据，便可以删除缓存  <br>
     * (2) 如果该档位有订单数据，那么就从该批次的订单中剔除掉已经成交的或者取消的订单
     */
    @Override
    public void orderTradeUpdateEvent(String exchangeName, OrderTradeUpdateEvent event) {
        int orderLevel = orderBookDepthDistribution.getOrderLevelByUUid(event.getOrigClientOrderId());
        String cacheKey = OrderTradeUtil.buildLevelOrderTradeKey(exchangeName, event.getSymbol(), event.getSide(), orderLevel);
        log.info("ProfitMarketMakingSpotOrderTradeDataManager.orderTradeUpdateEvent: cacheKey is {} time {} event is {} ", cacheKey, System.currentTimeMillis(), event.toString());

        // 成交事件 → 更新库存跟踪器
        if (event.getExecutionType() == ExecutionType.TRADE
                && event.getQuantityLastFilledTrade() != null) {
            try {
                inventoryTracker.onOrderFilled(
                        event.getSymbol(),
                        event.getSide(),
                        new BigDecimal(event.getQuantityLastFilledTrade()));
            } catch (Exception e) {
                log.warn("InventoryTracker.onOrderFilled failed, symbol={}", event.getSymbol(), e);
            }
        }

        if (event.getExecutionType() == ExecutionType.TRADE || event.getExecutionType() == ExecutionType.CANCELED) {
            OwnOrderBook cacheOwnerOrder = orderTradePool.get(cacheKey);
            if (cacheOwnerOrder == null) {
                log.warn("ProfitMarketMakingSpotOrderTradeDataManager.orderTradeUpdateEvent.tradeOrCanceled {} cacheOrder is null why?", cacheKey);
                return;
            }
            log.info("ProfitMarketMakingSpotOrderTradeDataManager.orderTradeUpdateEvent: cacheKey is {} time {} cacheOrder is {} ", cacheKey, System.currentTimeMillis(), JacksonUtil.toJsonStr(cacheOwnerOrder));

            // 如果本身的订单池中没有该档位的订单，就直接删除缓存。
            List<OrderTradeUpdateEvent> eventList = cacheOwnerOrder.getOrderTradeUpdateEventList();
            if (CollectionUtils.isEmpty(eventList)) {
                orderTradePool.remove(cacheKey);
                return;
            }

            // 判断哪些订单可以删除
            List<OrderTradeUpdateEvent> newEventList = new ArrayList<>();
            for (OrderTradeUpdateEvent item : eventList) {
                if (!event.getOrigClientOrderId().equals(item.getOrigClientOrderId())) {
                    newEventList.add(item);
                }
            }

            // 如果订单队列里面的数据都完成或者被取消了，那么就删除缓存
            if (CollectionUtils.isEmpty(newEventList)) {
                orderTradePool.remove(cacheKey);
                return;
            }
            cacheOwnerOrder.setOrderTradeUpdateEventList(newEventList);
            orderTradePool.put(cacheKey, cacheOwnerOrder);
        }
    }

    public void buildInitOrder(String exchangeName, String symbol, OrderSide orderSide) {
        List<OrderLevel> orderLevelList = profitOrderPlaceStrategy.getOrderLevelList();
        orderLevelList.forEach((item) -> {
            String orderBookCache = OrderTradeUtil.buildLevelOrderTradeKey(exchangeName, symbol, orderSide, item.getLevel());
            OwnOrderBook ownOrderBook = orderTradePool.get(orderBookCache);
            if (ownOrderBook == null) {
                OrderTradeUpdateEvent newOrderTradeBuyEvent = OrderTradeUpdateEvent.createInitOrderTradeUpdateEvent(orderSide);
                String anchorPrice = (OrderSide.BUY.equals(orderSide)) ? String.valueOf(Integer.MIN_VALUE) : String.valueOf(Integer.MAX_VALUE);
                OwnOrderBook orderBook = new OwnOrderBook(anchorPrice, SystemClock.now(), List.of(newOrderTradeBuyEvent));
                orderTradePool.put(orderBookCache, orderBook);
            }
        });
    }

    public void putOrderIntoOrderPool(SpotOrderExecutionContext context, List<ExchangeOrder> sportOrderList) {
        Long createTime = SystemClock.now();
        String cacheKey = OrderTradeUtil.buildLevelOrderTradeKey(context.getExchangeName(), context.getSymbol(), context.getOrderSide(), context.getOrderLevel());
        OwnOrderBook cacheOrder = orderTradePool.get(cacheKey);

        List<OrderTradeUpdateEvent> orderTradeUpdateEventList = new ArrayList<>();
        sportOrderList.forEach(sportOrder -> {
            OrderTradeUpdateEvent orderTradeUpdateEvent = convertNewOrderParamsPlaceResultToEvent(sportOrder);
            orderTradeUpdateEventList.add(orderTradeUpdateEvent);
        });

        String anchorPrice = getAnchorPrice(context.getOrderSide(), context.getBookTickerEvent());
        if (cacheOrder != null) {
            // 订单创建时间比缓存中的订单创建时间晚，更新缓存
            if (createTime > cacheOrder.getCreateTime()) {
                OwnOrderBook ownOrderBook = new OwnOrderBook(anchorPrice, createTime, orderTradeUpdateEventList);
                orderTradePool.put(cacheKey, ownOrderBook);
            } else {
                log.warn("ProfitMarketMakingSpotOrderTradeDataManager.putNewOrderIntoOrderPool {} cacheOrder is not null why?", cacheKey);
            }
        } else {
            OwnOrderBook ownOrderBook = new OwnOrderBook(anchorPrice, createTime, orderTradeUpdateEventList);
            orderTradePool.put(cacheKey, ownOrderBook);
        }
    }

    private String getAnchorPrice(OrderSide orderSide, BookTickerEvent bookTickerEvent) {
        if (OrderSide.BUY.equals(orderSide)) {
            return bookTickerEvent.getBidPrice();
        } else {
            return bookTickerEvent.getAskPrice();
        }
    }

    private OrderTradeUpdateEvent convertNewOrderParamsPlaceResultToEvent(ExchangeOrder sportOrderParams) {
        OrderTradeUpdateEvent orderTradeUpdateEvent = new OrderTradeUpdateEvent();
        orderTradeUpdateEvent.setExecutionType(ExecutionType.NEW);
        orderTradeUpdateEvent.setSymbol(sportOrderParams.getSymbol());
        orderTradeUpdateEvent.setSide(sportOrderParams.getSide());
        orderTradeUpdateEvent.setEventTime(sportOrderParams.getEventTime());
        orderTradeUpdateEvent.setPrice(sportOrderParams.getPrice());
        orderTradeUpdateEvent.setOrigClientOrderId(sportOrderParams.getNewClientOrderId());
        orderTradeUpdateEvent.setOriginalQuantity(sportOrderParams.getQuantity());
        return orderTradeUpdateEvent;
    }
}
