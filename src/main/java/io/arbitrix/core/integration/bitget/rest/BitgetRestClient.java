package io.arbitrix.core.integration.bitget.rest;

import io.arbitrix.core.common.domain.ExchangeOrder;
import io.arbitrix.core.common.domain.Order;
import io.arbitrix.core.common.monitor.annotation.PercentilesMetrics;
import io.arbitrix.core.common.util.JacksonUtil;
import io.arbitrix.core.integration.bitget.rest.api.SpotOrderApi;
import io.arbitrix.core.integration.bitget.rest.dto.req.SpotCancelOrderReq;
import io.arbitrix.core.integration.bitget.rest.dto.req.SpotOpenOrderReq;
import io.arbitrix.core.integration.bitget.rest.dto.req.SpotPlaceOrderReq;
import io.arbitrix.core.integration.bitget.rest.dto.res.BaseRes;
import io.arbitrix.core.integration.bitget.rest.dto.res.SpotCancelOrderRes;
import io.arbitrix.core.integration.bitget.rest.dto.res.SpotOpenOrderRes;
import io.arbitrix.core.integration.bitget.rest.dto.res.SpotPlaceOrderRes;
import io.arbitrix.core.integration.bitget.rest.enums.TimeForceEnum;
import io.arbitrix.core.integration.bitget.util.BitgetUtil;
import io.arbitrix.core.utils.timewindow.CallLimiter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@Log4j2
public class BitgetRestClient {
    private final SpotOrderApi orderClient;
    private final CallLimiter cancelCallLimit = new CallLimiter();
    private final CallLimiter placeOrderCallLimit = new CallLimiter();

    public BitgetRestClient(SpotOrderApi orderClient) {
        this.orderClient = orderClient;
    }

    @PercentilesMetrics
    public List<Order> getOpenOrders(List<String> symbolPairs) {
        List<Order> result = new ArrayList<>();
        try {
            for (String symbol : symbolPairs) {
                SpotOpenOrderReq openOrderReq = SpotOpenOrderReq.builder().symbol(BitgetUtil.symbolFromArbitrixToBitgetSpot(symbol)).build();
                BaseRes<List<SpotOpenOrderRes>> openOrdersRes = orderClient.openOrders(openOrderReq);
                if (!openOrdersRes.isSuccess()) {
                    log.error("BitgetRestClient.getOpenOrders error, response is {}", JacksonUtil.toJsonStr(openOrdersRes));
                    continue;
                }
                List<SpotOpenOrderRes> openOrders = openOrdersRes.getData();
                if (CollectionUtils.isEmpty(openOrders)) {
                    log.debug("BitgetRestClient.getOpenOrders data is empty, response is {}", JacksonUtil.toJsonStr(openOrdersRes));
                    continue;
                }
                for (SpotOpenOrderRes openOrder : openOrders) {
                    Order order = openOrder.convert2Orders();
                    if (Objects.nonNull(order)) {
                        result.add(order);
                    }
                }
            }

        } catch (Exception e) {
            log.error("BitgetRestClient.getOpenOrders error", e);
        }
        return result;
    }

    @PercentilesMetrics
    public boolean cancel(String symbol, String clientOrderId) {
        SpotCancelOrderReq cancelOrderReq = SpotCancelOrderReq.builder()
                .clientOid(clientOrderId)
                .symbol(BitgetUtil.symbolFromArbitrixToBitgetSpot(symbol)).build();
        cancelCallLimit.increment();
        if (cancelCallLimit.getSum() > 10) {
            log.warn("BitgetRestClient.cancel.callLimiter.getSum() > 10");
            return false;
        }
        try {
            BaseRes<SpotCancelOrderRes> spotCancelOrderResBaseRes = orderClient.cancelOrder(cancelOrderReq);
            if (!spotCancelOrderResBaseRes.isSuccess()) {
                log.error("BitgetRestClient.cancel error, response is {}", JacksonUtil.toJsonStr(spotCancelOrderResBaseRes));
                return false;
            }
        } catch (Exception e) {
            BaseRes errorRes = parseCanDecodeError(e);
            if (Objects.nonNull(errorRes)) {
                if (errorRes.isOrderNotExist()) {
                    log.error("BitgetRestClient.cancel error, order not exist, request is {},result is {}", cancelOrderReq, errorRes);
                } else {
                    log.error("BitgetRestClient.cancel error, request is {},result is {}", cancelOrderReq, errorRes);
                }
            } else {
                log.error("BitgetRestClient.cancel error", e);
            }

            return false;
        }
        return true;
    }

    @PercentilesMetrics
    public void placeOrder(ExchangeOrder sportOrder) {
        placeOrderCallLimit.increment();
        if (placeOrderCallLimit.getSum() > 10) {
            log.warn("BitgetRestClient.placeOrder.callLimiter.getSum() > 10");
            return;
        }
        SpotPlaceOrderReq spotPlaceOrderReq = SpotPlaceOrderReq.builder()
                .symbol(BitgetUtil.symbolFromArbitrixToBitgetSpot(sportOrder.getSymbol()))
                .side(sportOrder.getSide().name().toLowerCase())
                .orderType(BitgetUtil.convertOrderType(sportOrder.getType()))
                // only maker
                .force(TimeForceEnum.POST_ONLY.getCode())
                .price(sportOrder.getPrice())
                .quantity(sportOrder.getQuantity())
                .clientOrderId(sportOrder.getNewClientOrderId())
                .build();
        try {
            BaseRes<SpotPlaceOrderRes> result = orderClient.placeOrder(spotPlaceOrderReq);
            if (result.isSuccess()) {
                log.info("BitgetRestClient.placeOrder success, result is {}", result);
            } else {
                log.error("BitgetRestClient.placeOrder error, result is {}", result);
            }
        } catch (Exception e) {
            BaseRes errorRes = parseCanDecodeError(e);
            if (Objects.nonNull(errorRes)) {
                if (errorRes.isInsufficientBalance()) {
                    log.error("BitgetRestClient.placeOrder error, insufficient balance, request is {},result is {}", spotPlaceOrderReq, errorRes);
                } else {
                    log.error("BitgetRestClient.placeOrder error, request is {},result is {}", spotPlaceOrderReq, errorRes);
                }
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    private BaseRes parseCanDecodeError(Exception e) {
        if (e instanceof HttpClientErrorException.BadRequest) {
            return JacksonUtil.fromJson(((HttpClientErrorException.BadRequest) e).getResponseBodyAsString(), BaseRes.class);
        }
        return null;
    }
}
