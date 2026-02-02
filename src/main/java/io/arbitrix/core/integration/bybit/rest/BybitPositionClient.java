package io.arbitrix.core.integration.bybit.rest;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import io.arbitrix.core.integration.bybit.rest.api.PositionApi;
import io.arbitrix.core.integration.bybit.rest.dto.req.PositionLeverageReq;
import io.arbitrix.core.integration.bybit.rest.dto.res.BaseRestRes;
import io.arbitrix.core.common.util.JacksonUtil;

/**
 * @author jonathan.ji
 */
@Log4j2
@Component
public class BybitPositionClient {

    private final PositionApi positionApi;

    public BybitPositionClient(PositionApi positionApi) {
        this.positionApi = positionApi;
    }

    public void setLeverage(String category, String symbol, String buyLeverage, String sellLeverage) {
        PositionLeverageReq body = new PositionLeverageReq(category, symbol, buyLeverage, sellLeverage);
        try {
            BaseRestRes<String> res = positionApi.setLeverage(body);
            if (res.isSuccess()) {
                log.info("BybitPositionClient.setLeverage success, body is {}", JacksonUtil.toJsonStr(body));
            } else {
                log.error("BybitPositionClient.setLeverage error, body is {} res is {}", JacksonUtil.toJsonStr(body), JacksonUtil.toJsonStr(res));
            }
        } catch (Exception ex) {
            log.error("BybitPositionClient.setLeverage error, body is {}", JacksonUtil.toJsonStr(body), ex);
        }
    }
}
