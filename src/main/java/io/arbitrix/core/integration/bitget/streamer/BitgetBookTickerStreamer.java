package io.arbitrix.core.integration.bitget.streamer;

import io.arbitrix.core.common.util.TrackingUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.CollectionUtils;
import io.arbitrix.core.common.enums.ExchangeNameEnum;
import io.arbitrix.core.common.event.BookTickerEvent;
import io.arbitrix.core.common.monitor.WSSMonitor;
import io.arbitrix.core.integration.bitget.wss.BitgetWebSocketClient;
import io.arbitrix.core.integration.bitget.wss.dto.req.SubscribeReq;
import io.arbitrix.core.integration.bitget.wss.dto.res.BookTicker;
import io.arbitrix.core.integration.bitget.wss.dto.res.WsBaseRes;
import io.arbitrix.core.strategy.base.action.BookTickerEventListener;
import io.arbitrix.core.strategy.base.condition.ExchangeConditional;
import io.arbitrix.core.utils.ExchangeMarketOpenUtilV2;
import io.arbitrix.core.common.util.JacksonUtil;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

/**
 * subscribe book ticker
 */
@Log4j2
@ExchangeConditional(exchangeName = "BITGET")
@AllArgsConstructor
public class BitgetBookTickerStreamer {
    private final List<BookTickerEventListener> quotesListenerList;
    private final ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil;
    private final BitgetWebSocketClient bitgetWebSocketClient;

    @PostConstruct
    public void startBookTickerStreaming() {
        List<String> symbolList = exchangeMarketOpenUtil.getSymbolPairs(ExchangeNameEnum.BITGET);
        if (CollectionUtils.isEmpty(symbolList)) {
            log.warn("BitgetBookTickerStreamer.startBookTickerStreaming.symbolList is empty");
            return;
        }
        List<SubscribeReq> subscribeReqs = symbolList.stream()
                .map(SubscribeReq::ticker)
                .collect(Collectors.toList());
        bitgetWebSocketClient.subscribe(subscribeReqs, response -> {
            try {
                WSSMonitor.receiveBookTickerEvent(ExchangeNameEnum.BITGET.name());
                WsBaseRes<SubscribeReq, List<BookTicker>> resMsg = JacksonUtil.from(response, new TypeReference<>() {
                });
                if (!resMsg.isSuccess()) {
                    log.warn("BitgetBookTickerStreamer.startBookTickerStreaming.error:{}", JacksonUtil.toJsonStr(resMsg));
                    return;
                }
                List<BookTickerEvent> bookTickerEvents = BookTicker.convert2BookTickerEventList(resMsg.getData());
                if (CollectionUtils.isEmpty(bookTickerEvents)) {
                    log.debug("BitgetBookTickerStreamer.startBookTickerStreaming.bookTickerEvents is empty and ignore");
                    return;
                }
                quotesListenerList.forEach(quotesListener -> {
                    bookTickerEvents.forEach(bookTickerEvent -> quotesListener.onBookTicker(ExchangeNameEnum.BITGET.name(), bookTickerEvent));
                });
            } finally {
                TrackingUtils.clearTrace();
            }
        });
        log.info("BitgetBookTickerStreamer.startBookTickerStreaming.subscribeReqs:{}", JacksonUtil.toJsonStr(subscribeReqs));
    }
}
