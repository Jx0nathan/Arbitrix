package io.arbitrix.core.integration.binance.rest;

import com.binance.connector.client.impl.SpotClientImpl;
import com.binance.connector.client.impl.spot.Market;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import io.arbitrix.core.integration.binance.rest.dto.res.ServerTimeResponse;
import io.arbitrix.core.common.util.JacksonUtil;

/**
 * @author jonathan.ji
 */
@Component
@Log4j2
public class BinanceMarketClient {
    private final Market marketClient;

    public BinanceMarketClient(SpotClientImpl spotClient) {
        this.marketClient = spotClient.createMarket();
    }

    public ServerTimeResponse serverTime() {
        try {
            return JacksonUtil.from(marketClient.time(), ServerTimeResponse.class);
        } catch (Exception e) {
            log.error("BinanceMarketClient serverTime error", e);
        }
        return null;
    }

}