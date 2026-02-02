package io.arbitrix.core.integration.bybit.rest;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import io.arbitrix.core.integration.bybit.rest.api.MarketApi;
import io.arbitrix.core.integration.bybit.rest.dto.res.BaseRestRes;
import io.arbitrix.core.integration.bybit.rest.dto.res.ServerTimeRes;
import io.arbitrix.core.integration.bybit.rest.dto.res.SportTickerInfoRes;
import io.arbitrix.core.integration.bybit.rest.dto.res.SportTickerInfoResDetail;
import io.arbitrix.core.common.util.JacksonUtil;

@Component
@Log4j2
public class BybitMarketRestClient {
    private final MarketApi marketApi;

    public BybitMarketRestClient(MarketApi marketApi) {
        this.marketApi = marketApi;
    }

    /**
     * 查詢最新行情信息
     */
    public SportTickerInfoRes getTickerInfo(String category, String symbol) {
        try {
            BaseRestRes<SportTickerInfoRes> tickerInfoRes = marketApi.getTickerInfo(category, symbol);
            if (!tickerInfoRes.isSuccess()) {
                log.error("BybitMarketRestClient.getTickerInfo error, response {}", JacksonUtil.toJsonStr(tickerInfoRes));
                return null;
            }
            return tickerInfoRes.getResult();
        } catch (Exception e) {
            log.error("BybitMarketRestClient.getTickerInfo error", e);
        }
        return null;
    }

    public ServerTimeRes serverTime() {
        try {
            BaseRestRes<ServerTimeRes> serverTimeResBaseRestRes = marketApi.serverTime();
            if (!serverTimeResBaseRestRes.isSuccess()) {
                log.error("BybitMarketRestClient.serverTime error, response {}", JacksonUtil.toJsonStr(serverTimeResBaseRestRes));
                return null;
            }
            return serverTimeResBaseRestRes.getResult();
        } catch (Exception e) {
            log.error("BybitMarketRestClient.serverTime error", e);
        }
        return null;
    }
}