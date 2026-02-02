package io.arbitrix.core.integration.binance.ping;

import com.binance.connector.client.impl.SpotClientImpl;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import io.arbitrix.core.integration.binance.rest.dto.req.ListenKey;
import io.arbitrix.core.common.util.JacksonUtil;

import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jonathan.ji
 */
@Component
@Log4j2
public class BinanceListenKeyService {
    private final SpotClientImpl spotClient;
    private volatile String listenKey;
    private final AtomicInteger initCount = new AtomicInteger(0);

    public BinanceListenKeyService(SpotClientImpl spotClient) {
        this.spotClient = spotClient;
    }

    /**
     * 生成ListenKey, 开始一个新的数据流。除非发送keepalive, 否则数据流于60分钟后关闭
     */
    public String createListenKey() {
        int num = initCount.addAndGet(1);
        if (num > 1) {
            log.error("BinanceListenKeyService.createListenKey initCount is {}", num);
            return "";
        }

        try {
            String responseListenKey = spotClient.createUserData().createListenKey();
            ListenKey listenKeyResponse = JacksonUtil.from(responseListenKey, ListenKey.class);
            this.listenKey = listenKeyResponse.getListenKey();
        } catch (Exception ex) {
            log.error("BinanceListenKeyService.createListenKey error", ex);
        }
        return listenKey;
    }

    /**
     * 有效期延长至本次调用后60分钟,建议每30分钟发送一个ping
     */
    public void extendListenKey() {
        if (listenKey == null || listenKey.isEmpty()) {
            log.debug("BinanceListenKeyService.extendListenKey listenKey is null or empty");
            return;
        }

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("listenKey", listenKey);
        try {
            String extendListenResponse = spotClient.createUserData().extendListenKey(parameters);
            log.info("BinanceListenKeyService.extendListenKey extendListenResponse is {}", extendListenResponse);
        } catch (Exception ex) {
            log.error("BinanceListenKeyService.extendListenKey error", ex);
        }
    }
}