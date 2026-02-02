package io.arbitrix.core.facade.market;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import io.arbitrix.core.common.domain.SymbolLimitInfo;
import io.arbitrix.core.common.event.BookTickerEvent;
import io.arbitrix.core.integration.bitget.rest.BitgetMarketClient;
import io.arbitrix.core.integration.bitget.rest.dto.res.ServerTimeRes;

import java.util.Objects;

@Log4j2
@Component("market-BITGET")
public class BitgetMarketAction implements MarketAction {
    private final BitgetMarketClient bitgetMarketClient;

    public BitgetMarketAction(BitgetMarketClient bitgetMarketClient) {
        this.bitgetMarketClient = bitgetMarketClient;
    }
    @Override
    public SymbolLimitInfo getSymbolLimitInfo(String symbol, String category) {
        // TODO 2023/11/21 待实现
        return null;
    }

    @Override
    public String getServerTime() {
        ServerTimeRes serverTimeResponse = bitgetMarketClient.serverTime();
        if (Objects.isNull(serverTimeResponse)) {
            return null;
        }
        return serverTimeResponse.getServerTime();
    }

    @Override
    public BookTickerEvent lastTicker(String symbol) {
        // TODO 2024/3/21 待实现
        return null;
    }
}
