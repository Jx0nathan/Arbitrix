package io.arbitrix.core.integration.okx.rest;

import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;
import io.arbitrix.core.integration.okx.config.OkxProperties;
import io.arbitrix.core.integration.okx.rest.dto.req.OkxAccountTradeFeeResponse;
import io.arbitrix.core.common.util.JacksonUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jonathan.ji
 */
@Component
@Log4j2
public class OkxAccountClient extends UriAndHeaderEncoder {
    private static final String TRADE_FEE_PATH = "/api/v5/account/trade-fee";

    public OkxAccountClient(OkxProperties okxProperties) {
        super(okxProperties);
    }

    /**
     * https://www.okx.com/docs-v5/zh/#trading-account-rest-api-get-the-maximum-loan-of-instrument
     *
     * @param instType 产品类型，SPOT：币币，MARGIN：币币杠杆，SWAP：永续合约，FUTURES：交割合约，OPTION：期权，非必传
     * @param instId   产品ID，如 BTC-USDT 仅适用于instType为币币/币币杠杆，非必传
     */
    public OkxAccountTradeFeeResponse getAccountTradeFee(int index, String instType, String instId) {
        try {
            Map<String, String> params = new HashMap<>(2);
            params.put("instType", instType);
            params.put("instId", instId);
            String uri = super.buildUrl(TRADE_FEE_PATH, params);

            HttpRequestBase httpRequest = super.encode("get", uri, "", String.valueOf(index));
            return this.httpClient.execute(httpRequest, this::decode);
        } catch (Exception ex) {
            log.error("AccountFeeClient.getAccountFee error is {}", ex.getMessage());
        }
        return null;
    }

    private OkxAccountTradeFeeResponse decode(HttpResponse httpResponse) throws IOException {
        HttpEntity entity = httpResponse.getEntity();
        String entityString = EntityUtils.toString(entity);
        return JacksonUtil.from(entityString, OkxAccountTradeFeeResponse.class);
    }
}
