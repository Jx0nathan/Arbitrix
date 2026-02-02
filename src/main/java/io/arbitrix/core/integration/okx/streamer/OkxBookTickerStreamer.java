package io.arbitrix.core.integration.okx.streamer;

import org.springframework.web.socket.WebSocketMessage;
import io.arbitrix.core.common.enums.ExchangeNameEnum;
import io.arbitrix.core.integration.okx.wss.dto.req.SubscribeArg;
import io.arbitrix.core.integration.okx.wss.dto.req.SubscribeRequest;
import io.arbitrix.core.integration.okx.wsshandler.OkxPublicWebsocketAbstractHandler;
import io.arbitrix.core.utils.ExchangeMarketOpenUtilV2;

import java.util.ArrayList;
import java.util.List;


/**
 * 暂时用不到
 * @author jonathan.ji
 */
public class OkxBookTickerStreamer extends OkxPublicWebsocketAbstractHandler {
    private final ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil;

    public OkxBookTickerStreamer(ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil) {
        this.exchangeMarketOpenUtil = exchangeMarketOpenUtil;
    }

    @Override
    public SubscribeRequest getSubscribeRequest() {
        List<String> instIdArray = exchangeMarketOpenUtil.getSymbolPairs(ExchangeNameEnum.OKX);
        List<SubscribeArg> subscribeArgList = new ArrayList<>();
        for (String str : instIdArray) {
            SubscribeArg subscribeArg = SubscribeArg.builder().instId(str).channel("tickers").build();
            subscribeArgList.add(subscribeArg);
        }
        return new SubscribeRequest("subscribe", subscribeArgList);
    }

    @Override
    public void handleMessageForChannel(WebSocketMessage<?> message) {

    }
}
