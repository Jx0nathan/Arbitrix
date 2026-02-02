package io.arbitrix.core.integration.okx.wsshandler;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import io.arbitrix.core.integration.okx.LoginRequestSign;
import io.arbitrix.core.integration.okx.streamer.OKXWalletStreamer;
import io.arbitrix.core.integration.okx.utils.OkxWSSUtil;
import io.arbitrix.core.integration.okx.wss.dto.req.LoginRequest;
import io.arbitrix.core.integration.okx.wss.dto.res.OkxSubscribeAccount;
import io.arbitrix.core.common.util.JacksonUtil;

import static io.arbitrix.core.integration.okx.wss.constant.OkxWssConstant.*;

@Log4j2
public class OkxAccountWebsocketHandler extends OkxPrivateWebsocketAbstractHandler {
    private final LoginRequestSign loginRequestSign;
    private final int priority;
    private final OKXWalletStreamer okxWalletStreamer;

    public OkxAccountWebsocketHandler(LoginRequestSign loginRequestSign, int priority, OKXWalletStreamer okxWalletStreamer) {
        this.loginRequestSign = loginRequestSign;
        this.priority = priority;
        this.okxWalletStreamer = okxWalletStreamer;
    }

    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) throws Exception {
        log.info("OkxAccountWebsocketHandler.afterConnectionEstablished priority: {}, session: {}", priority, session);
        LoginRequest loginRequest = loginRequestSign.sign(priority);
        String loginJson = JacksonUtil.toJsonStr(loginRequest);
        log.info("OkxAccountWebsocketHandler.loginJson priority: {}, loginJson: {}", priority, loginJson);
        session.sendMessage(new TextMessage(loginJson));
    }

    @Override
    public void handleMessage(@NotNull WebSocketSession session, @NotNull WebSocketMessage<?> message) throws Exception {
        if (OkxWSSUtil.isPongMessage(message)) {
            log.debug("ignore pong message priority: {}, session: {}", priority, session);
            return;
        }
        log.info("OkxAccountWebsocketHandler.handleMessage.session is {}  priority: {}, message: {}", session, priority, message);
        JsonNode wssResponse = JacksonUtil.from(message.getPayload().toString(), JsonNode.class);
        if (wssResponse.has(EVENT) && EVENT_LOGIN.equals(wssResponse.get(EVENT).asText())) {
            // 登入成功，注册频道
            if (LOGIN_SUCCESS.equals(wssResponse.get(CODE).asText())) {
                subscribeToAccount(session);
            } else {
                log.warn("OkxAccountWebsocketHandler.handleMessage.login failed session: {} priority: {} message: {}", session, priority, message);
            }
        } else if (wssResponse.has(EVENT) && EVENT_SUBSCRIBE.equals(wssResponse.get(EVENT).asText())) {
            log.info("priority: {},Subscribed to {}", priority, wssResponse.get("arg").get("channel").asText());
        }

        // 处理个人订单的推送
        if (isChannelAccount(wssResponse)) {
            OkxSubscribeAccount okxSubscribeAccount = JacksonUtil.from(message.getPayload().toString(), OkxSubscribeAccount.class);
            okxWalletStreamer.startOrderStatusStreaming(okxSubscribeAccount, priority);
        }
    }

    @Override
    public void handleTransportError(@NotNull WebSocketSession session, @NotNull Throwable exception) {
        log.info("OkxAccountWebsocketHandler.handleTransportError priority: {}, session: {}, exception: {}", priority, session, exception);
    }

    @Override
    public void afterConnectionClosed(@NotNull WebSocketSession session, @NotNull CloseStatus closeStatus) {
        log.info("OkxAccountWebsocketHandler.afterConnectionClosed priority: {}, session: {}, closeStatus: {}", priority, session, closeStatus);
    }

    private boolean isChannelAccount(JsonNode jsonNode) {
        return jsonNode.has(ARG)
                && jsonNode.has(DATA)
                && jsonNode.get(ARG).has(CHANNEL)
                && ACCOUNT_CHANNEL.equals(jsonNode.get(ARG).get(CHANNEL).asText());
    }
}
