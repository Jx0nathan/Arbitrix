package io.arbitrix.core.integration.bitget.wss;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import io.arbitrix.core.common.enums.ExchangeNameEnum;
import io.arbitrix.core.integration.bitget.config.BitgetProperties;
import io.arbitrix.core.integration.bitget.wss.dto.req.SubscribeReq;
import io.arbitrix.core.integration.bitget.wss.listener.SubscriptionListener;
import io.arbitrix.core.utils.ExchangeMarketOpenUtilV2;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
@Log4j2
public class BitgetWebSocketClient {
    private volatile BitGetWebSocketConnection bitGetWebSocketConnection;
    private final BitgetProperties bitgetProperties;
    private final ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil;
    private final Lock createClientLock = new ReentrantLock();

    public BitgetWebSocketClient(BitgetProperties bitgetProperties, ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil) {
        this.bitgetProperties = bitgetProperties;
        this.exchangeMarketOpenUtil = exchangeMarketOpenUtil;
        if (this.exchangeMarketOpenUtil.exchangeCanStart(ExchangeNameEnum.BITGET)) {
            createWebSocketApiClient();
        } else {
            log.info("Bitget is not open, do not create wsClient");
        }
    }

    private void createWebSocketApiClient() {
        log.info("BitgetWebSocketClient.createWebSocketApiClient");
        createClientLock.lock();
        try {
            // 链接之后自带心跳功能,不用单独实现心跳
            this.bitGetWebSocketConnection = BitGetWebSocketConnection.builder()
                    .pushUrl(bitgetProperties.getWsBaseUrl())
                    .apiKey(bitgetProperties.getApiKey())
                    .secretKey(bitgetProperties.getSecretKey())
                    .passPhrase(bitgetProperties.getPassPhrase())
                    .isLogin(true)
                    .errorListener(data -> log.error("BitgetWebSocketClient.errorListener error: {}", data))
                    .build();
        } finally {
            createClientLock.unlock();
        }
    }

    public void subscribe(List<SubscribeReq> subscribeReqs, SubscriptionListener listener) {
        bitGetWebSocketConnection.subscribe(subscribeReqs, listener);
    }
}
