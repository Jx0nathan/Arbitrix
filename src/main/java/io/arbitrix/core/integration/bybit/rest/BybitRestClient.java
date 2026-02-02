package io.arbitrix.core.integration.bybit.rest;

import io.arbitrix.core.common.domain.ExchangeOrder;
import io.arbitrix.core.common.domain.Order;
import io.arbitrix.core.common.monitor.annotation.PercentilesMetrics;
import io.arbitrix.core.common.util.JacksonUtil;
import io.arbitrix.core.integration.bybit.rest.api.SpotOrderApi;
import io.arbitrix.core.integration.bybit.rest.dto.req.PlaceOrderReq;
import io.arbitrix.core.integration.bybit.rest.dto.req.SpotBatchCancelOrderReq;
import io.arbitrix.core.integration.bybit.rest.dto.req.SpotCancelOrderReq;
import io.arbitrix.core.integration.bybit.rest.dto.res.*;
import io.arbitrix.core.integration.bybit.rest.enums.Category;
import io.arbitrix.core.integration.bybit.rest.enums.TimeForceEnum;
import io.arbitrix.core.integration.bybit.rest.exception.BybitCancelAllOrdersException;
import io.arbitrix.core.integration.bybit.util.BybitUtil;
import io.arbitrix.core.utils.timewindow.CallLimiter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Component
@Log4j2
public class BybitRestClient {
    private final SpotOrderApi orderClient;

    @Value("${bybit.api.cancel.limit:250}")
    private Long cancelLimit;
    private CallLimiter cancelCallLimiter;

    @Value("${bybit.api.cancel-batch.limit:250}")
    private Long cancelBatchLimit;
    private CallLimiter cancelBatchCallLimiter;

    @Value("${bybit.api.place.limit:250}")
    private Long placeLimit;
    private CallLimiter placeOrderCallLimiter;

    public BybitRestClient(SpotOrderApi orderClient) {
        this.orderClient = orderClient;
    }

    @PostConstruct
    public void init() {
        cancelCallLimiter = new CallLimiter(cancelLimit.intValue(), 1000);
        placeOrderCallLimiter = new CallLimiter(placeLimit.intValue(), 1000);
        cancelBatchCallLimiter = new CallLimiter(cancelBatchLimit.intValue(), 1000);
    }

    @PercentilesMetrics
    public List<Order> getOpenOrders(List<String> symbolPairs, String category) {
        List<Order> result = new ArrayList<>();
        try {
            for (String symbol : symbolPairs) {
                BaseRestRes<SpotOpenOrderRes> openOrdersRes = orderClient.openOrders(category, symbol);
                if (!openOrdersRes.isSuccess()) {
                    log.error("BybitRestClient.getOpenOrders error, symbol {} response is {}", symbol, JacksonUtil.toJsonStr(openOrdersRes));
                    continue;
                }
                SpotOpenOrderRes openOrders = openOrdersRes.getResult();
                if (Objects.isNull(openOrders) || CollectionUtils.isEmpty(openOrders.getList())) {
                    log.debug("BybitRestClient.getOpenOrders data is empty, symbol is {} response is {}", symbol, JacksonUtil.toJsonStr(openOrdersRes));
                    continue;
                }
                for (SpotOpenOrderInfo openOrder : openOrders.getList()) {
                    Order order = openOrder.convert2Orders();
                    if (Objects.nonNull(order)) {
                        result.add(order);
                    }
                }
            }
        } catch (Exception e) {
            log.error("BybitRestClient.getOpenOrders error, request {}", JacksonUtil.toJsonStr(symbolPairs), e);
        }
        return result;
    }

    @PercentilesMetrics
    public boolean cancel(String symbol, String category, String clientOrderId) {
        log.info("BybitRestClient.cancel start, symbol is {}, category is {}, clientOrderId is {}", symbol, category, clientOrderId);
        if (StringUtils.isEmpty(clientOrderId)) {
            log.warn("BybitRestClient.cancel clientOrderId is empty, symbol is {}", symbol);
            return true;
        }
        SpotCancelOrderReq cancelOrderReq = SpotCancelOrderReq.builder()
                .category(category)
                .clientOrderId(clientOrderId)
                .symbol(symbol).build();
        cancelCallLimiter.increment();
        if (cancelCallLimiter.getSum() > cancelLimit) {
            log.warn("BybitRestClient.cancel.callLimiter.getSum() > {}", cancelLimit);
            return false;
        }
        try {
            BaseRestRes<SpotCancelOrderRes> spotCancelOrderResBaseRestRes = orderClient.cancelOrder(cancelOrderReq);
            if (!spotCancelOrderResBaseRestRes.isSuccess()) {
                if (spotCancelOrderResBaseRestRes.isOrderNotExist()) {
                    log.warn("BybitRestClient.cancel order not exist, symbol:{},clientOrderId:{}, response is {}", symbol, clientOrderId, JacksonUtil.toJsonStr(spotCancelOrderResBaseRestRes));
                    return true;
                }
                log.error("BybitRestClient.cancel unknown error,symbol:{},clientOrderId:{}, response is {}", symbol, clientOrderId, JacksonUtil.toJsonStr(spotCancelOrderResBaseRestRes));
                return false;
            } else {
                log.info("BybitRestClient.cancel success, symbol:{},clientOrderId:{}, response is {}", symbol, clientOrderId, JacksonUtil.toJsonStr(spotCancelOrderResBaseRestRes));
            }
        } catch (Exception e) {
            log.error("BybitRestClient.cancel throw exception", e);
            return false;
        }
        return true;
    }

    @PercentilesMetrics
    public boolean cancelBatch(String category, List<Order> orders) {
        log.info("BybitRestClient.cancelBatch start, category is {}, orders is {}", category, JacksonUtil.toJsonStr(orders));
        if (CollectionUtils.isEmpty(orders)) {
            log.debug("BybitRestClient.cancelBatch orders is empty");
            return true;
        }
        cancelBatchCallLimiter.increment();
        if (cancelBatchCallLimiter.getSum() > cancelBatchLimit) {
            log.warn("BybitRestClient.cancelBatch.callLimiter.getSum() > {}", cancelBatchLimit);
            return false;
        }
        try {
            SpotBatchCancelOrderReq spotBatchCancelOrderReq = SpotBatchCancelOrderReq.convertFromOrders(category, orders);
            BaseRestRes<SpotBatchCancelOrderRes> spotBatchCancelOrderResBaseRestRes = orderClient.cancelBatchOrder(spotBatchCancelOrderReq);
            if (!spotBatchCancelOrderResBaseRestRes.isSuccess()) {
                log.error("BybitRestClient.cancelBatch error, request is {}, response is {}", JacksonUtil.toJsonStr(spotBatchCancelOrderReq), JacksonUtil.toJsonStr(spotBatchCancelOrderResBaseRestRes));
                return false;
            } else {
                SpotBatchCancelOrderExtInfoRes extInfoRes = JacksonUtil.fromJson(JacksonUtil.toJsonStr(spotBatchCancelOrderResBaseRestRes.getRetExtInfo()), SpotBatchCancelOrderExtInfoRes.class);
                if (extInfoRes.isSuccess()) {
                    log.info("BybitRestClient.cancelBatch success, request is {}, response is {}", JacksonUtil.toJsonStr(spotBatchCancelOrderReq), JacksonUtil.toJsonStr(spotBatchCancelOrderResBaseRestRes));
                } else {
                    log.warn("BybitRestClient.cancelBatch some order cancel fail request is {}, response is {}", JacksonUtil.toJsonStr(spotBatchCancelOrderReq), JacksonUtil.toJsonStr(spotBatchCancelOrderResBaseRestRes));
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("BybitRestClient.cancelBatch throw exception", e);
            return false;
        }
        return true;
    }

    @PercentilesMetrics
    public void placeOrder(ExchangeOrder sportOrder, String category) {
        log.info("BybitRestClient.placeOrder start, request is {}", JacksonUtil.toJsonStr(sportOrder));
        placeOrderCallLimiter.increment();
        if (placeOrderCallLimiter.getSum() > placeLimit) {
            log.warn("BybitRestClient.placeOrder.callLimiter.getSum() > {}", placeLimit);
            return;
        }
        try {
            PlaceOrderReq spotPlaceOrderReq = PlaceOrderReq.builder()
                    .category(category)
                    .symbol(sportOrder.getSymbol())
                    .side(BybitUtil.convertSideOut(sportOrder.getSide()))
                    .orderType(BybitUtil.convertOrderType(sportOrder.getType()))
                    .timeInForce(TimeForceEnum.POST_ONLY.getCode())
                    .price(sportOrder.getPrice())
                    .qty(sportOrder.getQuantity())
                    .clientOrderId(sportOrder.getNewClientOrderId())
                    .reduceOnly(sportOrder.getReduceOnly())
                    .build();
            BaseRestRes<SpotPlaceOrderRes> result = orderClient.placeOrder(spotPlaceOrderReq);
            if (result.isSuccess()) {
                log.info("BybitRestClient.placeOrder success, result is {}", result);
            } else {
                if (result.isInsufficientBalance()) {
                    log.error("BybitRestClient.placeOrder insufficient balance, request is {} result is {}", JacksonUtil.toJsonStr(sportOrder), JacksonUtil.toJsonStr(result));
                } else {
                    log.error("BybitRestClient.placeOrder error, request is {} result is {}", JacksonUtil.toJsonStr(sportOrder), JacksonUtil.toJsonStr(result));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean canUseBatchCancel(String category) {
        return Category.isLinear(category);
    }

    public void cancelAllOpenOrders(String category, List<String> symbolPairs) {
        log.info("BybitRestClient.cancelAllOpenOrders start, category is {}, symbolPairs is {}", category, JacksonUtil.toJsonStr(symbolPairs));
        if (CollectionUtils.isEmpty(symbolPairs)) {
            log.info("BybitRestClient.cancelAllOpenOrders symbolPairs is empty");
            return;
        }
        try {
            for (String symbol : symbolPairs) {
                BaseRestRes<SpotCancelAllOrderRes> cancelAllOrderRes = orderClient.cancelAllopenOrders(category, symbol);
                if (cancelAllOrderRes.isSuccess()) {
                    log.info("BybitRestClient.cancelAllOpenOrders success, category is {}, symbol is {}", category, symbol);
                } else {
                    String message = String.format("category is %s, symbol is %s, response is %s", category, symbol, JacksonUtil.toJsonStr(cancelAllOrderRes));
                    log.error("BybitRestClient.cancelAllOpenOrders error ", new BybitCancelAllOrdersException(message));
                }
            }
        } catch (Exception e) {
            log.error("BybitRestClient.cancelAllOpenOrders throw exception,category is {}, symbolPairs is {}", category, JacksonUtil.toJsonStr(symbolPairs), new BybitCancelAllOrdersException(e));
        }
    }
}
