package io.arbitrix.core.integration.okx.rest;

import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;
import io.arbitrix.core.common.monitor.annotation.PercentilesMetrics;
import io.arbitrix.core.integration.okx.config.OkxProperties;
import io.arbitrix.core.integration.okx.rest.dto.req.OkxCancelOrderResponse;
import io.arbitrix.core.integration.okx.rest.dto.res.OkxCancelOrderRequest;
import io.arbitrix.core.integration.okx.utils.OkxClientOrderIdUtil;
import io.arbitrix.core.utils.timewindow.CallLimiter;
import io.arbitrix.core.common.util.JacksonUtil;

@Component
@Log4j2
public class OkxCancelOrderClient extends UriAndHeaderEncoder {
    private static final String PATH = "/api/v5/trade/cancel-order";
    private final CallLimiter callLimiter = new CallLimiter(200, 2 * 1000);

    public OkxCancelOrderClient(OkxProperties okxProperties) {
        super(okxProperties);
    }

    @PercentilesMetrics
    public boolean cancel(String clientOrderId, String symbol) {
        if (clientOrderId == null) {
            log.warn("OkxCancelOrderClient.cancel.is.null");
            return false;
        }

        callLimiter.increment();
        if (callLimiter.getSum() > 200) {
            log.warn("OkxCancelOrderClient.postOrder.callLimiter.getSum() > 200");
            return false;
        }

        OkxCancelOrderRequest cancelRequest = new OkxCancelOrderRequest();
        String clientId = OkxClientOrderIdUtil.convert2OKX(clientOrderId);
        cancelRequest.setClOrdId(clientId);
        cancelRequest.setInstId(symbol);
        try {
            String requestJson = JacksonUtil.toJsonStr(cancelRequest);
            HttpRequestBase httpRequest = super.encode("POST", PATH, requestJson, clientId);
            HttpResponse response = httpClient.execute(httpRequest);
            String entityString = EntityUtils.toString(response.getEntity());
            OkxCancelOrderResponse okxCancelOrderResponse = JacksonUtil.from(entityString, OkxCancelOrderResponse.class);
            if (okxCancelOrderResponse.getCode() != 0) {
                log.error("OkxCancelOrderClient.cancel cancelRequest is {} response is {} clientOrderId is {}",
                        JacksonUtil.toJsonStr(cancelRequest), entityString, clientOrderId);
                return false;
            }
            return true;
        } catch (Exception ex) {
            log.error("OkxCancelOrderClient.cancel error is {}", ex.getMessage());
            return false;
        }
    }
}
