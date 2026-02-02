package io.arbitrix.core.integration.bitget.rest;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import io.arbitrix.core.integration.bitget.rest.api.MarketApi;
import io.arbitrix.core.integration.bitget.rest.dto.res.BaseRes;
import io.arbitrix.core.integration.bitget.rest.dto.res.ServerTimeRes;
import io.arbitrix.core.common.util.JacksonUtil;

@Component
@Log4j2
public class BitgetMarketClient {
    private final MarketApi marketApi;

    public BitgetMarketClient(MarketApi marketApi) {
        this.marketApi = marketApi;
    }

    public ServerTimeRes serverTime() {
        try {
            BaseRes<ServerTimeRes> serverTimeRes = marketApi.serverTime();
            if (!serverTimeRes.isSuccess()) {
                log.error("BitgetMarketClient.serverTime error, response is {}", JacksonUtil.toJsonStr(serverTimeRes));
                return null;
            }
            return serverTimeRes.getData();
        } catch (Exception e) {
            log.error("BitgetMarketClient.serverTime error", e);
        }
        return null;
    }
}