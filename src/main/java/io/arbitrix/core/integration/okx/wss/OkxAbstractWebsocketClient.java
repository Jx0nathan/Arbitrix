package io.arbitrix.core.integration.okx.wss;

import lombok.extern.log4j.Log4j2;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public abstract class OkxAbstractWebsocketClient {

    protected final Map<WebSocketHandler, WebSocketSession> sessionHandlerMap = new ConcurrentHashMap<>();

    private static final String SIMULATED_TRADING_HEADER = "x-simulated-trading";

    protected OkxAbstractWebsocketClient() {
    }

    @PostConstruct
    public void startWebSocketConnection() {
        getWebSocketHandlers().forEach((webSocketHandler) -> {
            log.info("Connecting to websocket, handler class name:{}", webSocketHandler.getClass().getSimpleName());
            doConnection(webSocketHandler);
        });
    }

    protected abstract List<? extends WebSocketHandler> getWebSocketHandlers();

    protected abstract boolean isTestTrading();

    protected abstract String getWsBaseUrl();

    protected void doConnection(WebSocketHandler webSocketHandler) {
        WebSocketClient client = new StandardWebSocketClient();
        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        if (isTestTrading()) {
            // 模拟盘的请求的header里面需要添加 "x-simulated-trading: 1"
            headers.add(SIMULATED_TRADING_HEADER, "1");
        }
        try {
            WebSocketSession webSocketSession = client.doHandshake(webSocketHandler, headers, URI.create(getWsBaseUrl())).get();
            sessionHandlerMap.put(webSocketHandler, webSocketSession);
        } catch (Exception e) {
            log.error("{}.startWebSocketConnection error: ", this.getClass().getSimpleName(), e);
            throw new RuntimeException(e);
        }
    }

    public void ping() {
        sessionHandlerMap.forEach((handler, session) -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage("ping"));
                } else {
                    doConnection(handler);
                }
            } catch (Exception e) {
                log.error("{}.ping error: ", this.getClass().getSimpleName(), e);
            }
        });
    }
}
