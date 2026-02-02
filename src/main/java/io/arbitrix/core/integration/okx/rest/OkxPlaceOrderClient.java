package io.arbitrix.core.integration.okx.rest;

import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;
import io.arbitrix.core.common.domain.ExchangeOrder;
import io.arbitrix.core.common.monitor.annotation.PercentilesMetrics;
import io.arbitrix.core.integration.okx.config.OkxProperties;
import io.arbitrix.core.integration.okx.rest.dto.req.OkxOrderResponse;
import io.arbitrix.core.integration.okx.rest.dto.res.OkxOrderRequest;
import io.arbitrix.core.integration.okx.rest.enums.OkxOrderType;
import io.arbitrix.core.integration.okx.utils.OkxClientOrderIdUtil;
import io.arbitrix.core.integration.okx.utils.OkxUtil;
import io.arbitrix.core.utils.SystemClock;
import io.arbitrix.core.utils.timewindow.CallLimiter;
import io.arbitrix.core.common.util.JacksonUtil;

import java.io.IOException;

/**
 * @author jonathan.ji
 */
@Component
@Log4j2
public class OkxPlaceOrderClient extends UriAndHeaderEncoder {
    private static final String PATH = "/api/v5/trade/order";
    private final CallLimiter callLimiter = new CallLimiter(200, 2 * 1000);

    public OkxPlaceOrderClient(OkxProperties okxProperties) {
        super(okxProperties);
    }

    @PercentilesMetrics
    public void placeOrder(ExchangeOrder sportOrder) {
        OkxOrderRequest okxOrderRequest = this.conventOkxOrderRequest(sportOrder);
        placeOrder(okxOrderRequest);
    }

    /**
     * 限速：60次/2s
     */
    public void placeOrder(OkxOrderRequest okxOrderRequest) {
        callLimiter.increment();
        if (callLimiter.getSum() > 200) {
            log.warn("OkxPlaceOrderClient.postOrder.callLimiter.getSum() > 200");
            return;
        }

        try {
            long startTime = SystemClock.now();
            String body = JacksonUtil.toJsonStr(okxOrderRequest);
            log.info("OkxPlaceOrderClient.placeOrder request is {}", body);
            HttpRequestBase httpRequest = super.encode("Post", PATH, body, okxOrderRequest.getClOrdId());

            OkxOrderResponse okxOrderResponse = httpClient.execute(httpRequest, this::decode);
            if (okxOrderResponse != null && okxOrderResponse.getCode() == 0) {
                log.info("OkxPlaceOrderClient.placeOrder success response is {}", JacksonUtil.toJsonStr(okxOrderResponse.getData()));
            } else {
                log.error("OkxPlaceOrderClient.placeOrder error is {}", JacksonUtil.toJsonStr(okxOrderResponse));
            }
            log.info("PureMarketMakingSpotWorker.newOrderByRestAndWss.okxPlaceOrderClient.placeOrder.cost.time:{}", SystemClock.now() - startTime);
        } catch (Exception ex) {
            log.error("OkxPlaceOrderClient.placeOrder error is {}", ex.getMessage(), ex);
        }
    }

    private OkxOrderRequest conventOkxOrderRequest(ExchangeOrder sportOrder) {
        OkxOrderRequest okxOrderRequest = new OkxOrderRequest();
        okxOrderRequest.setInstId(sportOrder.getSymbol());
        okxOrderRequest.setSide(OkxUtil.convertToOrderSide(sportOrder.getSide().name()));
        okxOrderRequest.setOrdType(OkxOrderType.POST_ONLY);
        // 简单交易模式
        okxOrderRequest.setTdMode("cash");
        okxOrderRequest.setSz(sportOrder.getQuantity());
        okxOrderRequest.setPx(sportOrder.getPrice());

        // 字母（区分大小写）与数字的组合，可以是纯字母、纯数字且长度要在1-32位之间
        String okxClientOrderId = OkxClientOrderIdUtil.convert2OKX(sportOrder.getNewClientOrderId());
        okxOrderRequest.setClOrdId(okxClientOrderId);
        return okxOrderRequest;
    }

    private OkxOrderResponse decode(HttpResponse httpResponse) throws IOException {
        HttpEntity entity = httpResponse.getEntity();
        String entityString = EntityUtils.toString(entity);
        return JacksonUtil.from(entityString, OkxOrderResponse.class);
    }
}
