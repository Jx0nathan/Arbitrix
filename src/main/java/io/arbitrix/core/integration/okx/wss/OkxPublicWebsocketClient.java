package io.arbitrix.core.integration.okx.wss;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import io.arbitrix.core.integration.okx.wsshandler.OkxPublicWebsocketLogic;
import io.arbitrix.core.strategy.base.condition.ExchangeConditional;

import java.util.List;

@Component
@Log4j2
@ExchangeConditional(exchangeName = "OKX")
public class OkxPublicWebsocketClient extends OkxAbstractWebsocketClient {

    private final List<OkxPublicWebsocketLogic> okxPublicWebsocketLogics;
    @Value("${okx.testTrading:false}")
    private Boolean testTrading;

    @Value("${okx.mbx.wsBaseUrl}")
    private String wsBaseUrl;

    public OkxPublicWebsocketClient(List<OkxPublicWebsocketLogic> okxPublicWebsocketLogics) {
        this.okxPublicWebsocketLogics = okxPublicWebsocketLogics;
    }

    @Override
    protected List<? extends WebSocketHandler> getWebSocketHandlers() {
        return okxPublicWebsocketLogics;
    }

    @Override
    protected boolean isTestTrading() {
        return testTrading;
    }

    @Override
    protected String getWsBaseUrl() {
        return wsBaseUrl;
    }
}
