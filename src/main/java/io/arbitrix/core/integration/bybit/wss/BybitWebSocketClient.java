package io.arbitrix.core.integration.bybit.wss;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import io.arbitrix.core.common.enums.ExchangeNameEnum;
import io.arbitrix.core.common.enums.WSStreamType;
import io.arbitrix.core.integration.bybit.rest.enums.Category;
import io.arbitrix.core.integration.bybit.wss.listener.WSSMessageListener;
import io.arbitrix.core.integration.bybit.config.BybitProperties;
import io.arbitrix.core.utils.ExchangeMarketOpenUtilV2;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
@Log4j2
public class BybitWebSocketClient {
    private volatile BybitWebSocketConnection publicWebSocketConnection;
    private volatile BybitWebSocketConnection privateWebSocketConnection;
    private final BybitProperties bybitProperties;
    private final ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil;
    private final Lock createCOnnectionLock = new ReentrantLock();

    public BybitWebSocketClient(BybitProperties bybitProperties, ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil) {
        this.bybitProperties = bybitProperties;
        this.exchangeMarketOpenUtil = exchangeMarketOpenUtil;
        if (this.exchangeMarketOpenUtil.exchangeCanStart(ExchangeNameEnum.BYBIT)) {
            createWebSocketApiClient();
        } else {
            log.info("Bybit is not open, do not create wsClient");
        }
    }

    private void createWebSocketApiClient() {
        log.info("BybitWebSocketClient.createWebSocketApiClient");
        createCOnnectionLock.lock();
        try {
            String category = bybitProperties.getCategory();
            this.publicWebSocketConnection = BybitWebSocketConnection.builder()
                    .properties(bybitProperties)
                    .baseUrl((Category.SPOT.getCode().equalsIgnoreCase(category)) ? bybitProperties.getWsPublicBaseUrl() : bybitProperties.getWsFuturePublicBaseUrl())
                    .needAuth(false)
                    .errorListener(data -> log.error("BybitBookTickerStreamer.publicWebSocketConnection.errorListener error: {}", data))
                    .wsStreamType(WSStreamType.PUBLIC)
                    .build();
            this.privateWebSocketConnection = BybitWebSocketConnection.builder()
                    .properties(bybitProperties)
                    .baseUrl(bybitProperties.getWsPrivateBaseUrl())
                    .needAuth(true)
                    .errorListener(data -> log.error("BybitBookTickerStreamer.privateWebSocketConnection.errorListener error: {}", data))
                    .wsStreamType(WSStreamType.PRIVATE)
                    .build();
        } finally {
            createCOnnectionLock.unlock();
        }
    }

    public void subscribePublic(List<String> topics, WSSMessageListener listener) {
        publicWebSocketConnection.subscribe(topics, listener);
    }

    public void subscribePrivate(List<String> topics, WSSMessageListener listener) {
        privateWebSocketConnection.subscribe(topics, listener);
    }
}
