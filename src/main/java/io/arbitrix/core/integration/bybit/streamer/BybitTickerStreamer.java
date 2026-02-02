package io.arbitrix.core.integration.bybit.streamer;

import io.arbitrix.core.common.util.TrackingUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import io.arbitrix.core.common.enums.ExchangeNameEnum;
import io.arbitrix.core.common.event.BookTickerEvent;
import io.arbitrix.core.common.monitor.WSSMonitor;
import io.arbitrix.core.facade.market.ByBitMarketAction;
import io.arbitrix.core.integration.bybit.wss.BybitWebSocketClient;
import io.arbitrix.core.integration.bybit.wss.dto.req.OrderbookDepthReq;
import io.arbitrix.core.integration.bybit.wss.dto.req.TickerReq;
import io.arbitrix.core.integration.bybit.wss.dto.res.OrderbookDepthRes;
import io.arbitrix.core.integration.bybit.wss.dto.res.TickerRes;
import io.arbitrix.core.integration.bybit.wss.dto.res.WSStreamBaseRes;
import io.arbitrix.core.strategy.base.action.BookTickerEventListener;
import io.arbitrix.core.strategy.base.condition.ExchangeConditional;
import io.arbitrix.core.strategy.base.condition.ExecuteStrategyConditional;
import io.arbitrix.core.utils.ExchangeMarketOpenUtilV2;
import io.arbitrix.core.common.util.JacksonUtil;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * subscribe ticker <br>
 * @author Charles Meng
 */
@Log4j2
@Component
@AllArgsConstructor
@ExchangeConditional(exchangeName = "BYBIT")
@ExecuteStrategyConditional(executeStrategyName = "moving_price")
public class BybitTickerStreamer {
    private final ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil;
    private final BybitWebSocketClient bybitWebSocketClient;
    private final ByBitMarketAction byBitMarketAction;

    @PostConstruct
    public void startTickerStreaming() {
        List<String> symbolList = exchangeMarketOpenUtil.getSymbolPairs(ExchangeNameEnum.BYBIT);
        if (CollectionUtils.isEmpty(symbolList)) {
            log.warn("BybitTickerStreamer.startTickerStreaming.symbolList is empty");
            return;
        }
        List<String> tickerTopics = symbolList.stream()
                .map(symbol -> TickerReq.withSymbol(symbol).getTopic())
                .collect(Collectors.toList());
        bybitWebSocketClient.subscribePublic(tickerTopics, response -> {
            try {
                WSSMonitor.receiveBookTickerEvent(ExchangeNameEnum.BYBIT.name());
                WSStreamBaseRes<List<TickerRes>> resMsg = JacksonUtil.from(response, new TypeReference<>() {
                });

                List<TickerRes> data = resMsg.getData();
                if (CollectionUtils.isEmpty(data)) {
                    log.debug("BybitTickerStreamer.tickerStreaming.onEvent is empty");
                    return ;
                }
                for (TickerRes ticker : data) {
                    byBitMarketAction.onTickerEvent(ticker.convert2BookTickerEvent());
                }
            } finally {
                TrackingUtils.clearTrace();
            }
        });
        log.info("BybitBookTickerStreamer.startBookTickerStreaming.tickerTopics:{}", JacksonUtil.toJsonStr(tickerTopics));
    }
}
