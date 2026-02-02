package io.arbitrix.core.strategy.future_pure_market_making.data;

import io.arbitrix.core.common.util.EnvUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import io.arbitrix.core.common.domain.ExchangeOrder;
import io.arbitrix.core.common.domain.Order;
import io.arbitrix.core.common.enums.ExchangeNameEnum;
import io.arbitrix.core.common.enums.ExecutionType;
import io.arbitrix.core.common.enums.OrderSide;
import io.arbitrix.core.common.event.OrderTradeUpdateEvent;
import io.arbitrix.core.strategy.base.action.AbstractOrderTradeDataManager;
import io.arbitrix.core.strategy.base.action.OrderTradeUpdateListener;
import io.arbitrix.core.strategy.base.condition.ExecuteStrategyConditional;
import io.arbitrix.core.strategy.base.openorder.*;
import io.arbitrix.core.utils.ExchangeMarketOpenUtilV2;
import io.arbitrix.core.utils.OrderTradeUtil;
import io.arbitrix.core.utils.executor.NamedThreadFactory;
import io.arbitrix.core.common.util.JacksonUtil;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.arbitrix.core.utils.ExchangeMarketOpenUtilV2.EXCHANGE;

/**
 * @author jonathan.ji
 */
@Log4j2
@Component("futurePureMarketMakingOrderTradeDataManager")
@ExecuteStrategyConditional(executeStrategyName = "future_pure_market_making")
public class FuturePureMarketMakingOrderTradeDataManager extends AbstractOrderTradeDataManager implements OrderTradeUpdateListener {
    private final ExchangeNameEnum exchangeNameEnum;
    private final ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil;
    private final ScheduledExecutorService SCHEDULER = new ScheduledThreadPoolExecutor(1,
            new NamedThreadFactory("FuturePureMarketMakingOrderTradeDataManagerRunner", true));

    private final Map<String, OrderTradeUpdateEvent> orderTradePool = new ConcurrentHashMap<>();

    public FuturePureMarketMakingOrderTradeDataManager(BinanceOpenOrderRunner binanceOpenOrderRunner,
                                                       OkxOpenOrderRunner okxOpenOrderRunner,
                                                       BitgetOpenOrderRunner bitgetOpenOrderRunner,
                                                       BybitOpenOrderRunner bybitOpenOrderRunner,
                                                       ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil) {

        super(exchangeMarketOpenUtil, binanceOpenOrderRunner, okxOpenOrderRunner, bitgetOpenOrderRunner, bybitOpenOrderRunner);
        this.exchangeMarketOpenUtil = exchangeMarketOpenUtil;
        this.exchangeNameEnum = ExchangeNameEnum.getExchangeName(EnvUtil.getProperty(EXCHANGE));
    }

    public Map<String, OrderTradeUpdateEvent> getOrderTradePool() {
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

        // 订单取消的定时任务，遍历所有的挂单，如果这个订单的价格和最优价格不一致，那么就取消这个订单
        Function<List<Order>, List<Order>> cancelOrderProcessor = orders -> {
            List<Order> cancelOrderList = new ArrayList<>();
            for (Order order : orders) {
                String cacheKey = OrderTradeUtil.buildOrderTradeKey(exchangeNameEnum.getValue(), order.getSymbol(), order.getSide());
                OrderTradeUpdateEvent cacheOrder  = orderTradePool.get(cacheKey);
                if (cacheOrder != null && cacheOrder.getPrice() != null) {
                    // 如果价格不相同便取消订单
                    if (isDifferentPrice(cacheOrder.getPrice(), order.getPrice())) {
                        cancelOrderList.add(order);
                    } else {
                        log.info("ExchangeOpenOrderRunner.getCancelOrderListBaseOnBestPrice order price is same,exchange:{},orderPrice:{}", exchangeNameEnum.getValue(), order.getPrice());
                    }
                }
            }

            // 判断订单缓存池是否可以清除
            Map<OrderSide, List<Order>> groupedMap = orders.stream().collect(Collectors.groupingBy(Order::getSide));
            cancelOrderList.addAll(processOrderPollClear(groupedMap.get(OrderSide.BUY)));
            cancelOrderList.addAll(processOrderPollClear(groupedMap.get(OrderSide.SELL)));

            return cancelOrderList;
        };
        super.startOpenOrderRunner(cancelOrderProcessor);
    }

    private List<Order> processOrderPollClear(List<Order> orderList) {
        if (CollectionUtils.isEmpty(orderList)) {
            return Collections.emptyList();
        }

        List<Order> sortedList = orderList.stream()
                .sorted(Comparator.comparing(Order::getTime).reversed()).collect(Collectors.toList());

        Order order = sortedList.get(0);
        String cacheKey = OrderTradeUtil.buildOrderTradeKey(exchangeNameEnum.getValue(), order.getSymbol(), order.getSide());
        OrderTradeUpdateEvent cacheOrder = orderTradePool.get(cacheKey);
        List<Order> needClearOrders = new ArrayList<>();
        if (cacheOrder != null && isDifferentPrice(cacheOrder.getPrice(), order.getPrice())) {
            // 这边只选择清除缓存的原因 是害怕有并发的原因导致的数据污染。订单已经执行，但是还未查询到，如果覆盖，缓存变成脏数据
            OrderTradeUpdateEvent orderTradeUpdateEvent = orderTradePool.remove(cacheKey);
            if (Objects.nonNull(orderTradeUpdateEvent)) {
                log.info("PureMarketMakingSpotOrderTradeDataManager.orderPoolClear remove cacheKey is {} orderTradeUpdateEvent is {}", cacheKey, JacksonUtil.toJsonStr(orderTradeUpdateEvent));
                needClearOrders.add(Order.builder().
                        symbol(orderTradeUpdateEvent.getSymbol()).
                        side(orderTradeUpdateEvent.getSide()).
                        clientOrderId(orderTradeUpdateEvent.getOrigClientOrderId()).build());
            }
        }
        return needClearOrders;
    }

    private static boolean isDifferentPrice(String anchorPrice, String price) {
        return new BigDecimal(anchorPrice).compareTo(new BigDecimal(price)) != 0;
    }

    /**
     * 监听订单成交事件，如果已经成交或者取消，就从缓存池中删除
     */
    @Override
    public void orderTradeUpdateEvent(String exchangeName, OrderTradeUpdateEvent event) {
        String cacheKey = OrderTradeUtil.buildOrderTradeKey(exchangeName, event.getSymbol(), event.getSide());
        log.info("AccountDataManager.orderTradeUpdateEvent: cacheKey is {} time {} event is {} ", cacheKey, System.currentTimeMillis(), event.toString());

        if (event.getExecutionType() == ExecutionType.TRADE || event.getExecutionType() == ExecutionType.CANCELED) {
            OrderTradeUpdateEvent cacheOrder = orderTradePool.get(cacheKey);
            if (cacheOrder == null) {
                log.warn("AccountDataManager.orderTradeUpdateEvent.tradeOrCanceled {} cacheOrder is null why?", cacheKey);
                return;
            }
            log.info("AccountDataManager.orderTradeUpdateEvent: cacheKey is {} time {} cacheOrder is {} ", cacheKey, System.currentTimeMillis(), JacksonUtil.toJsonStr(cacheOrder));
            if (event.getNewClientOrderId().equals(cacheOrder.getOrigClientOrderId())) {
                orderTradePool.remove(cacheKey);
            }
        }
    }

    public void buildInitOrder(String exchangeName, String symbol, OrderSide orderSide) {
        String symbolKey = OrderTradeUtil.buildOrderTradeKey(exchangeName, symbol, orderSide);
        OrderTradeUpdateEvent item = orderTradePool.get(symbolKey);
        if (item == null) {
            OrderTradeUpdateEvent newOrderTradeBuyEvent = OrderTradeUpdateEvent.createInitOrderTradeUpdateEvent(orderSide);
            orderTradePool.put(symbolKey, newOrderTradeBuyEvent);
        }
    }

    public void putOrderIntoOrderPool(String exchangeName, ExchangeOrder futureOrder) {
        OrderTradeUpdateEvent orderTradeUpdateEvent = this.convertNewOrderParamsPlaceResultToEvent(futureOrder);
        String cacheKey = OrderTradeUtil.buildOrderTradeKey(exchangeName, orderTradeUpdateEvent.getSymbol(), orderTradeUpdateEvent.getSide());
        OrderTradeUpdateEvent cacheOrder = orderTradePool.get(cacheKey);
        if (cacheOrder != null) {
            // 订单创建时间比缓存中的订单创建时间晚，更新缓存
            if (orderTradeUpdateEvent.getEventTime() > cacheOrder.getEventTime()) {
                orderTradePool.put(cacheKey, orderTradeUpdateEvent);
            } else {
                log.warn("FuturePureMarketMakingOrderTradeDataManager.putOrderIntoOrderPool {} cacheOrder is not null why?", cacheKey);
            }
        } else {
            orderTradePool.put(cacheKey, orderTradeUpdateEvent);
        }
    }

    private OrderTradeUpdateEvent convertNewOrderParamsPlaceResultToEvent(ExchangeOrder futureOrderParams) {
        OrderTradeUpdateEvent orderTradeUpdateEvent = new OrderTradeUpdateEvent();
        orderTradeUpdateEvent.setExecutionType(ExecutionType.NEW);
        orderTradeUpdateEvent.setSymbol(futureOrderParams.getSymbol());
        orderTradeUpdateEvent.setSide(futureOrderParams.getSide());
        orderTradeUpdateEvent.setEventTime(futureOrderParams.getEventTime());
        orderTradeUpdateEvent.setPrice(futureOrderParams.getPrice());
        orderTradeUpdateEvent.setOrigClientOrderId(futureOrderParams.getNewClientOrderId());
        orderTradeUpdateEvent.setOriginalQuantity(futureOrderParams.getQuantity());
        return orderTradeUpdateEvent;
    }
}
