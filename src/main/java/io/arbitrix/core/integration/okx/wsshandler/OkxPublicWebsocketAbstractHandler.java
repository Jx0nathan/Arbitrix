package io.arbitrix.core.integration.okx.wsshandler;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import io.arbitrix.core.integration.okx.wss.dto.req.SubscribeRequest;
import io.arbitrix.core.common.util.JacksonUtil;

/**
 * @author jonathan.ji
 */
@Component
@Log4j2
public abstract class OkxPublicWebsocketAbstractHandler implements OkxPublicWebsocketLogic {

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("{}.afterConnectionEstablished is: {}", this.getClass().getSimpleName(), session);
        SubscribeRequest subscribeRequest = this.getSubscribeRequest();
        session.sendMessage(new TextMessage(JacksonUtil.toJsonStr(subscribeRequest)));
    }

    /**
     * 封装订阅的请求参数
     *
     * @return 订阅请求参数
     */
    public abstract SubscribeRequest getSubscribeRequest();

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if ("pong".equals(message.getPayload())) {
            log.debug("ignore pong message");
            return;
        }
        log.debug("OkxWebsocketHandler.handleMessage.session is {}  message is {}", session, message.getPayload());
        handleMessageForChannel(message);
    }

    public abstract void handleMessageForChannel(WebSocketMessage<?> message);

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.info("handleTransportError session: {}", session, exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("afterConnectionClosed session: {}. closeStatus: {}", session, closeStatus);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
