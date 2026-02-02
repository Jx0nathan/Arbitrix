package io.arbitrix.core.integration.okx.rest;

import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import io.arbitrix.core.integration.okx.config.OkxProperties;
import io.arbitrix.core.integration.okx.rest.dto.req.ServerTime;
import io.arbitrix.core.integration.okx.rest.dto.req.ServerTimeResponse;
import io.arbitrix.core.integration.okx.rest.dto.req.TickerIndex;
import io.arbitrix.core.integration.okx.rest.dto.req.TickerIndexResponse;
import io.arbitrix.core.common.util.JacksonUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OKX获取公共数据
 *
 * @author jonathan.ji
 */
@Component
@Log4j2
public class OkxPublicDataClient extends UriAndHeaderEncoder {

    public static final String INDEX_TICKERS_PATH = "/api/v5/market/index-tickers";
    public static final String SERVER_TIME_PATH = "/api/v5/public/time";

    public OkxPublicDataClient(OkxProperties okxProperties) {
        super(okxProperties);
    }

    /**
     * 获取指数行情数据
     *
     * @param instId 指数，如 BTC-USD
     * @return 指数数据
     */
    public TickerIndex getTickerIndex(String instId) {
        try {
            Map<String, String> params = new HashMap<>(2);
            params.put("instId", instId);
            String uri = super.buildUrl(INDEX_TICKERS_PATH, params);
            HttpRequestBase httpRequest = super.encode("get", uri, "", String.valueOf(1));

            TickerIndexResponse tickerIndexResponse = httpClient.execute(httpRequest, response -> {
                HttpEntity entity = response.getEntity();
                return JacksonUtil.from(EntityUtils.toString(entity), TickerIndexResponse.class);
            });
            if (tickerIndexResponse != null && tickerIndexResponse.getCode() == 0) {
                List<TickerIndex> tickerIndices = tickerIndexResponse.getData();
                if (tickerIndices != null && tickerIndices.size() > 0) {
                    return tickerIndices.get(0);
                }
            }
            log.warn("OkxPublicDataClient.getTickerIndex error is {}", tickerIndexResponse);
        } catch (Exception ex) {
            log.error("OkxPublicDataClient.getTickerIndex error is {}", ex.getMessage());
        }
        return null;
    }

    public ServerTime getServerTime() {
        try {
            HttpRequestBase httpRequest = super.encode("get", SERVER_TIME_PATH, "", String.valueOf(1));

            ServerTimeResponse serverTimeResponse = httpClient.execute(httpRequest, response -> {
                HttpEntity entity = response.getEntity();
                return ServerTimeResponse.fromJson(EntityUtils.toString(entity));
            });
            if (serverTimeResponse != null && serverTimeResponse.getCode() == 0) {
                List<ServerTime> data = serverTimeResponse.getData();
                if (!CollectionUtils.isEmpty(data)) {
                    return data.get(0);
                }
            }
            log.error("OkxPublicDataClient.getTickerIndex error is {}", serverTimeResponse);
        } catch (Exception ex) {
            log.error("OkxPublicDataClient.getTickerIndex error is {}", ex.getMessage());
        }
        return null;
    }
}
