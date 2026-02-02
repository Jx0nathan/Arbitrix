package io.arbitrix.core.integration.binance.wss;

import com.binance.connector.client.exceptions.BinanceConnectorException;
import com.binance.connector.client.impl.WebSocketApiClientImpl;
import com.binance.connector.client.utils.signaturegenerator.HmacSignatureGenerator;
import lombok.extern.log4j.Log4j2;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import io.arbitrix.core.common.domain.ExchangeOrder;
import io.arbitrix.core.common.enums.OrderSide;
import io.arbitrix.core.common.enums.OrderType;
import io.arbitrix.core.common.monitor.annotation.PercentilesMetrics;
import io.arbitrix.core.integration.binance.config.BinanceProperties;
import io.arbitrix.core.integration.binance.wss.dto.res.OrderPlaceResponse;
import io.arbitrix.core.utils.timewindow.CallLimiter;
import io.arbitrix.core.common.util.JacksonUtil;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author jonathan.ji
 */
@Component
@Log4j2
public class BinanceWebSocketClient {
    private volatile WebSocketApiClientImpl wsApiClient;
    private final BinanceProperties binanceMbxProperties;
    private final Lock createClientLock = new ReentrantLock();
    private final CallLimiter callLimiter = new CallLimiter();

    public BinanceWebSocketClient(BinanceProperties binanceMbxProperties) {
        this.binanceMbxProperties = binanceMbxProperties;
        createWebSocketApiClient(binanceMbxProperties);
    }

    private void createWebSocketApiClient(BinanceProperties binanceMbxProperties) {
        HmacSignatureGenerator signatureGenerator = new HmacSignatureGenerator(binanceMbxProperties.getSecretKey());
        log.info("BinanceWebSocketClient.createWebSocketApiClient");
        createClientLock.lock();
        try {
            this.wsApiClient = new WebSocketApiClientImpl(binanceMbxProperties.getApiKey(), signatureGenerator, binanceMbxProperties.getWsRestBaseUrl());
        } finally {
            createClientLock.unlock();
        }
    }

    @PercentilesMetrics
    public void placeOrder(ExchangeOrder sportOrder) {
        placeOrder(sportOrder.getSymbol(), sportOrder.getType(), sportOrder.getNewClientOrderId(), sportOrder.getQuantity(), sportOrder.getPrice(), sportOrder.getSide());
    }

    public void placeOrder(String symbol, OrderType orderType, String newClientOrderId, String quantity, String price, OrderSide orderSide) {
        wsApiClient.connect((event) -> log.info("WebSocketOpenCallback.postOrder.Open.WebSocket: {}", event), (event) -> {
            try {
                log.info("WebSocketOpenCallback.postOrder.OnMessage.WebSocket: {}", event);
                OrderPlaceResponse orderPlaceResponse = JacksonUtil.from(event, OrderPlaceResponse.class);
                if (orderPlaceResponse != null && orderPlaceResponse.getStatus() != HttpStatus.OK.value()) {
                    // code=-2011,msg=Unknown order sent. 取消的订单已经被取消了或者不存在
                    // code=-2010,msg=Order would immediately match and take. post only 订单下单失败
                    log.error("WebSocketOpenCallback.postOrder.fail: {}", event);
                }
            } catch (Exception ex) {
                log.error("WebSocketOpenCallback.postOrder.OnMessage.exception: {} {}", ex, event);
            }
        }, (event) -> {
            log.warn("WebSocketOpenCallback.postOrder.Close.WebSocket: {}", event);
            createWebSocketApiClient(binanceMbxProperties);
        }, (event) -> createWebSocketApiClient(binanceMbxProperties));

        JSONObject optionalParams = new JSONObject();
        optionalParams.put("side", orderSide.name());
        optionalParams.put("price", price);
        optionalParams.put("quantity", quantity);
        optionalParams.put("newClientOrderId", newClientOrderId);
        optionalParams.put("newOrderRespType", "RESULT");

        callLimiter.increment();
        if (callLimiter.getSum() > 20) {
            log.warn("WebSocketOpenCallback.postOrder.callLimiter.getSum() > 20");
            return;
        }

        try {
            wsApiClient.trade().newOrder(symbol, orderSide.name(), orderType.name(), optionalParams);
        } catch (BinanceConnectorException e) {
            log.error("WebSocket.newOrder.BinanceConnectorException.WebSocket exception is", e);
            createWebSocketApiClient(binanceMbxProperties);
        }
    }

    @PercentilesMetrics
    public void cancelOrder(String symbol, String origClientOrderId) {
        if (origClientOrderId == null) {
            log.warn("WebSocketOpenCallback.cancelOrder.onReceive.WebSocket.OrigClientOrderId is null");
            return;
        }

        log.info("WebSocketOpenCallback.start.to.cancelOrder origClientOrderId is {}", origClientOrderId);
        wsApiClient.connect((event) -> log.info("WebSocketOpenCallback.cancelOrder.onReceive.WebSocket.Open: {}", event), (event) -> log.info("WebSocketOpenCallback.cancelOrder.onReceive.WebSocket.OnMessage: {}", event), (event) -> {
            log.info("WebSocketOpenCallback.cancelOrder.onReceive.WebSocket.Close: {}", event);
            createWebSocketApiClient(binanceMbxProperties);
        }, (event) -> createWebSocketApiClient(binanceMbxProperties));

        callLimiter.increment();
        if (callLimiter.getSum() > 20) {
            log.warn("WebSocketOpenCallback.postOrder.callLimiter.getSum() > 20");
            return;
        }

        JSONObject optionalParams = new JSONObject();
        optionalParams.put("origClientOrderId", origClientOrderId);
        try {
            wsApiClient.trade().cancelOrder(symbol, optionalParams);
        } catch (BinanceConnectorException e) {
            log.error("WebSocket.cancelOrder.BinanceConnectorException.WebSocket exception is", e);
            createWebSocketApiClient(binanceMbxProperties);
        }
    }

    @SuppressWarnings("unused")
    public void getOrder(String symbol, String orderId) {
        wsApiClient.connect((event) -> log.info("WebSocketOpenCallback.getOrder.OnMessage: {}", event));

        JSONObject optionalParams = new JSONObject();
        optionalParams.put("orderId", orderId);
        wsApiClient.trade().getOrder(symbol, optionalParams);
    }

    @SuppressWarnings("unused")
    public void cancelAllOrder(String symbol) {
        wsApiClient.connect((event) -> log.info("WebSocketOpenCallback.getOrder.OnMessage: {}", event));
        JSONObject optionalParams = new JSONObject();
        wsApiClient.trade().cancelAllOpenOrders(symbol, optionalParams);
    }
}
