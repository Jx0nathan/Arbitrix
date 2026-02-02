package io.arbitrix.core.integration.okx.wsshandler;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import io.arbitrix.core.integration.okx.LoginRequestSign;
import io.arbitrix.core.integration.okx.streamer.OkxOrderStatusStreamer;
import io.arbitrix.core.integration.okx.wss.dto.req.LoginRequest;
import io.arbitrix.core.integration.okx.wss.dto.req.SubscribeArg;
import io.arbitrix.core.integration.okx.wss.dto.req.SubscribeRequest;
import io.arbitrix.core.integration.okx.wss.dto.res.OkxSubscribeOrderInfo;
import io.arbitrix.core.strategy.base.condition.ExchangeConditional;
import io.arbitrix.core.common.util.JacksonUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.arbitrix.core.integration.okx.wss.constant.OkxWssConstant.*;

@Component
@Log4j2
@ExchangeConditional(exchangeName = "OKX")
@RequiredArgsConstructor
public class OkxPrivateWebsocketHandler01 implements OkxPrivateWebsocketLogic {
    // TODO 2023/11/22 参考OkxAccountStatusStreamer将三个order的handler合并成一个
    private final LoginRequestSign loginRequestSign;
    private final OkxOrderStatusStreamer okxOrderStatusStreamer;

    @Value("${okx.subscribe.channels:orders}")
    private String channelsToSubscribeTo;

    @Value("${symbolPairs:}")
    private String subscribeOrderInstIds;

    private static final int PRIORITY = 0;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("OkxPrivateWebsocketHandler01.afterConnectionEstablished is: {}", session);
        LoginRequest loginRequest = loginRequestSign.sign(PRIORITY);
        String loginJson = JacksonUtil.toJsonStr(loginRequest);
        log.info("OkxPrivateWebsocketHandler01.loginJson is: {}", loginJson);
        session.sendMessage(new TextMessage(loginJson));
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if ("pong".equals(message.getPayload())) {
            log.debug("ignore pong message");
            return;
        }
        log.info("OkxPrivateWebsocketHandler01.handleMessage.session is {}  message is {}", session, message.getPayload());
        JsonNode wssResponse = JacksonUtil.from(message.getPayload().toString(), JsonNode.class);
        if (wssResponse.has(EVENT) && EVENT_LOGIN.equals(wssResponse.get(EVENT).asText())) {
            // 登入成功，注册频道
            if (LOGIN_SUCCESS.equals(wssResponse.get(CODE).asText())) {
                if (!channelsToSubscribeTo.isBlank()) {
                    subscribeToChannels(session, channelsToSubscribeTo.split(","));
                }
            } else {
                log.warn("OkxPrivateWebsocketHandler01.Login.failed: {}", wssResponse);
            }
        } else if (wssResponse.has(EVENT) && EVENT_SUBSCRIBE.equals(wssResponse.get(EVENT).asText())) {
            log.info("Subscribed to " + wssResponse.get("arg").get("channel").asText());
        }

        // 处理个人订单的推送
        if (isOrderFromOrderChannel(wssResponse)) {
            OkxSubscribeOrderInfo okxSubscribeOrderInfo = JacksonUtil.from(message.getPayload().toString(), OkxSubscribeOrderInfo.class);
            okxOrderStatusStreamer.startOrderStatusStreaming(okxSubscribeOrderInfo);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.info("OkxPrivateWebsocketHandler01.handleTransportError session: {}", session, exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("OkxPrivateWebsocketHandler01.afterConnectionClosed session: {}. closeStatus: {}", session, closeStatus);
    }

    @Override
    public boolean supportsPartialMessages() {
        return true;
    }

    private void subscribeToChannels(WebSocketSession session, String[] channel) throws IOException {
        List<SubscribeArg> args = new ArrayList<>();
        for (String ch : channel) {
            // 处理订单的订阅请求
            if (ORDER_CHANNEL.equals(ch)) {
                Arrays.stream(subscribeOrderInstIds.split(","))
                        .map(instId ->
                                SubscribeArg.builder().instType("SPOT").channel(ch).instId(instId).build()).forEach(args::add);
            }

            // 处理账户的订阅请求
            if (ACCOUNT_CHANNEL.equals(ch)) {
                args.add(SubscribeArg.builder().instType("SPOT").channel(ch).build());
            }
        }
        SubscribeRequest subscribeRequest = SubscribeRequest.builder().op("subscribe").args(args).build();
        String subscribeJson = JacksonUtil.toJsonStr(subscribeRequest);
        session.sendMessage(new TextMessage(subscribeJson));
    }

    private boolean isOrderFromOrderChannel(JsonNode jsonNode) {
        return jsonNode.has(ARG)
                && jsonNode.has(DATA)
                && jsonNode.get(ARG).has(CHANNEL)
                && ORDER_CHANNEL.equals(jsonNode.get(ARG).get(CHANNEL).asText());
    }

    private boolean isChannelAccount(JsonNode jsonNode) {
        return jsonNode.has(ARG)
                && jsonNode.has(DATA)
                && jsonNode.get(ARG).has(CHANNEL)
                && ACCOUNT_CHANNEL.equals(jsonNode.get(ARG).get(CHANNEL).asText());
    }
}
