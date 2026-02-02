package io.arbitrix.core.integration.okx.wss;

import com.binance.connector.client.utils.RequestBuilder;
import com.binance.connector.client.utils.WebSocketCallback;
import com.binance.connector.client.utils.WebSocketConnection;
import com.binance.connector.client.utils.httpclient.WebSocketStreamHttpClientSingleton;
import lombok.extern.log4j.Log4j2;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.stereotype.Component;
import io.arbitrix.core.integration.okx.config.OkxProperties;
import io.arbitrix.core.integration.okx.wss.dto.req.LoginRequest;
import io.arbitrix.core.integration.okx.wss.dto.req.PlaceOrderRequest;
import io.arbitrix.core.common.domain.ExchangeOrder;
import io.arbitrix.core.integration.okx.LoginRequestSign;
import io.arbitrix.core.integration.okx.utils.OkxUtil;
import io.arbitrix.core.strategy.base.condition.ExchangeConditional;
import io.arbitrix.core.utils.timewindow.CallLimiter;
import io.arbitrix.core.common.util.JacksonUtil;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jonathan.ji
 */
@Component
@Log4j2
@ExchangeConditional(exchangeName = "OKX")
public class OkxWebSocketClient {
    private final OkHttpClient client = WebSocketStreamHttpClientSingleton.getHttpClient();
    private final WebSocketConnection connection;
    private final LoginRequestSign loginRequestSign;
    private final AtomicInteger firstLoginRequest = new AtomicInteger(0);
    private final CallLimiter callLimiter = new CallLimiter(200,2  * 1000);

    public OkxWebSocketClient(LoginRequestSign loginRequestSign, OkxProperties okxProperties) {
        this.loginRequestSign = loginRequestSign;

        Request request = RequestBuilder.buildWebSocketRequest(okxProperties.getWsRestBaseUrl());
        WebSocketCallback onOpenCallback = data -> {
            log.info("OkxWebSocketClient.onOpenCallback" + data);
        };
        WebSocketCallback onMessageCallback = data -> {
            log.info("OkxWebSocketClient.onMessageCallback" + data);
        };
        WebSocketCallback onClosingCallback = data -> {
            log.info("OkxWebSocketClient.onClosingCallback" + data);
            firstLoginRequest.set(0);
        };
        WebSocketCallback onFailureCallback = data -> {
            log.info("OkxWebSocketClient.onFailureCallback" + data);
            firstLoginRequest.set(0);
        };
        this.connection = new WebSocketConnection(onOpenCallback, onMessageCallback, onClosingCallback, onFailureCallback, request, client);
    }

    public void placeOrder(ExchangeOrder sportOrder) {
        callLimiter.increment();
        if (callLimiter.getSum() > 200) {
            log.warn("WebSocketOpenCallback.postOrder.callLimiter.getSum() > 200");
            return;
        }

        connection.connect();
        if (firstLoginRequest.getAndIncrement() == 0) {
            log.info("OkxWebSocketClient.firstLoginRequest");
            // 暂时没有用wss进行下单，如果使用了，请注意这个地方的逻辑，参数有问题。
            LoginRequest loginRequest = loginRequestSign.sign(0);
            connection.send(JacksonUtil.toJsonStr(loginRequest));
        }
        PlaceOrderRequest placeOrderRequest = this.conventPlaceOrderRequest(sportOrder);
        connection.send(JacksonUtil.toJsonStr(placeOrderRequest));
    }

    private PlaceOrderRequest conventPlaceOrderRequest(ExchangeOrder sportOrder) {
        PlaceOrderRequest.Arg arg = new PlaceOrderRequest.Arg();
        arg.setInstId(sportOrder.getSymbol());
        arg.setSide(OkxUtil.convertToOrderSide(sportOrder.getSide().name()).name().toLowerCase());
        arg.setOrdType("post_only");
        // 简单交易模式
        arg.setTdMode("cash");
        arg.setSz(sportOrder.getQuantity());
        arg.setPx(sportOrder.getPrice());

        // 字母（区分大小写）与数字的组合，可以是纯字母、纯数字且长度要在1-32位之间
        String okxClientOrderId = sportOrder.getNewClientOrderId().replaceAll("-", "");
        arg.setClOrdId(okxClientOrderId);

        PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest();
        placeOrderRequest.setOp("order");
        placeOrderRequest.setId(okxClientOrderId);
        placeOrderRequest.setArgs(List.of(arg));
        return placeOrderRequest;
    }
}
