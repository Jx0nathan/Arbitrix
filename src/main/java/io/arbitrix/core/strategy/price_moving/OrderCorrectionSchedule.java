package io.arbitrix.core.strategy.price_moving;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import io.arbitrix.core.common.domain.ExchangeOrder;
import io.arbitrix.core.common.domain.Order;
import io.arbitrix.core.common.enums.ExchangeNameEnum;
import io.arbitrix.core.integration.bybit.rest.BybitRestClient;
import io.arbitrix.core.integration.bybit.rest.enums.Category;
import io.arbitrix.core.utils.ExchangeMarketOpenUtilV2;
import io.arbitrix.core.utils.executor.MovingPriceCancelOrderExecutor;
import io.arbitrix.core.utils.executor.MovingPricePlaceOrderExecutor;
import io.arbitrix.core.utils.executor.NamedThreadFactory;
import io.arbitrix.core.common.util.JacksonUtil;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Log4j2
@Component
public class OrderCorrectionSchedule {
    private final ScheduledExecutorService SCHEDULER = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("OrderCorrectionSchedule", true));
    private final BybitRestClient bybitRestClient;
    private final ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil;
    private final PriceTierMoveService priceTierMoveService;
    private final String symbol;

    private final ThreadPoolExecutor PLACE_ORDER_EXECUTOR = MovingPricePlaceOrderExecutor.getInstance();
    private final ThreadPoolExecutor CANCEL_ORDER_EXECUTOR = MovingPriceCancelOrderExecutor.getInstance();

    public OrderCorrectionSchedule(BybitRestClient bybitRestClient, ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil, PriceTierMoveService priceTierMoveService) {
        this.bybitRestClient = bybitRestClient;
        this.exchangeMarketOpenUtil = exchangeMarketOpenUtil;
        this.priceTierMoveService = priceTierMoveService;

        List<String> symbolList = exchangeMarketOpenUtil.getSymbolPairs(ExchangeNameEnum.BYBIT);
        this.symbol = symbolList.get(0);
    }

    @PostConstruct
    public void start() {
        SCHEDULER.scheduleAtFixedRate(() -> {
            try {
                Map<String, Order> openOrderMap = this.getOpenOrderMap();
                Map<String, ExchangeOrder> exchangeOrderMap = priceTierMoveService.getPriceTierOrderMap();
                log.info("OrderCorrectionSchedule.openOrderMap.is:{},exchangeOrderMap.is:{}", JacksonUtil.toJsonStr(openOrderMap), JacksonUtil.toJsonStr(exchangeOrderMap));

                // 以订单薄为基准，查看是否有订单需要被创建，极端情况下会重复创建某一个订单
                List<ExchangeOrder> createOrderList = exchangeOrderMap.entrySet().stream()
                        .filter(entry -> !openOrderMap.containsKey(entry.getValue().getNewClientOrderId()))
                        .map(Map.Entry::getValue)
                        .collect(Collectors.toList());
                log.info("OrderCorrectionSchedule.createOrderList:{}", JacksonUtil.toJsonStr(createOrderList));
                createOrderList.forEach(order -> PLACE_ORDER_EXECUTOR.execute(() -> bybitRestClient.placeOrder(order, Category.SPOT.getCode())));

                // 以用户挂单为基准，查看是否有订单需要被取消。如果用户挂单不在订单薄中，则取消
                for (String clientOrderId : openOrderMap.keySet()) {
                    boolean inOrderMap = this.checkInOrderMap(clientOrderId, exchangeOrderMap);
                    if (!inOrderMap) {
                        log.info("OrderCorrectionSchedule.cancelOrder.clientOrderId:{}", clientOrderId);
                        CANCEL_ORDER_EXECUTOR.execute(() -> bybitRestClient.cancel(symbol, Category.SPOT.getCode(), clientOrderId));
                    }
                }
            } catch (Exception ex) {
                log.error("OrderCorrectionSchedule.error", ex);
            }
        }, 15000, 7000, TimeUnit.MILLISECONDS);
    }

    private boolean checkInOrderMap(String clientOrderId, Map<String, ExchangeOrder> exchangeOrderMap) {
        boolean result = false;
        for (String key : exchangeOrderMap.keySet()) {
            if (exchangeOrderMap.get(key).getNewClientOrderId().equals(clientOrderId)) {
                result = true;
                break;
            }
        }
        return result;
    }

    private Map<String, Order> getOpenOrderMap() {
        List<Order> orderList = bybitRestClient.getOpenOrders(Collections.singletonList(symbol), Category.SPOT.getCode());
        return orderList.stream()
                .collect(Collectors.toMap(Order::getClientOrderId, order -> order));
    }
}
