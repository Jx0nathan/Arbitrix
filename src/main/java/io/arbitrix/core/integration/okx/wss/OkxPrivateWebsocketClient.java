package io.arbitrix.core.integration.okx.wss;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import io.arbitrix.core.integration.okx.wsshandler.OkxPrivateWebsocketLogic;
import io.arbitrix.core.strategy.base.condition.ExchangeConditional;

import java.util.List;

@Component
@Log4j2
@ExchangeConditional(exchangeName = "OKX")
public class OkxPrivateWebsocketClient extends OkxAbstractWebsocketClient {
    private final List<OkxPrivateWebsocketLogic> okxPrivateWebsocketLogics;

    @Value("${okx.testTrading:false}")
    private Boolean testTrading;

    @Value("${okx.mbx.wsRestBaseUrl}")
    private String wsRestBaseUrl;


    public OkxPrivateWebsocketClient(List<OkxPrivateWebsocketLogic> okxPrivateWebsocketLogics) {
        this.okxPrivateWebsocketLogics = okxPrivateWebsocketLogics;
    }

    @Override
    protected List<? extends WebSocketHandler> getWebSocketHandlers() {
        return okxPrivateWebsocketLogics;
    }

    @Override
    protected boolean isTestTrading() {
        return testTrading;
    }

    @Override
    protected String getWsBaseUrl() {
        return wsRestBaseUrl;
    }
}
