package io.arbitrix.core.integration.okx.wsshandler;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import io.arbitrix.core.integration.okx.wss.dto.req.SubscribeArg;
import io.arbitrix.core.integration.okx.wss.dto.req.SubscribeRequest;
import io.arbitrix.core.common.util.JacksonUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static io.arbitrix.core.integration.okx.wss.constant.OkxWssConstant.*;

public abstract class OkxPrivateWebsocketAbstractHandler implements OkxPrivateWebsocketLogic {

    protected void subscribeToChannels(WebSocketSession session, String[] channel, List<String> symbols) throws IOException {
        List<SubscribeArg> args = new ArrayList<>();
        for (String ch : channel) {
            // 处理订单的订阅请求
            if (ORDER_CHANNEL.equals(ch)) {
                args.addAll(buildOrderSubscribeArgs(symbols));
            }

            // 处理账户的订阅请求
            if (ACCOUNT_CHANNEL.equals(ch)) {
                args.add(buildAccountSubscribeArg());
            }
        }
        SubscribeRequest subscribeRequest = SubscribeRequest.builder().op("subscribe").args(args).build();
        String subscribeJson = JacksonUtil.toJsonStr(subscribeRequest);
        session.sendMessage(new TextMessage(subscribeJson));
    }

    protected SubscribeArg buildAccountSubscribeArg() {
        return SubscribeArg.builder().instType("SPOT").channel(ACCOUNT_CHANNEL).build();
    }

    protected List<SubscribeArg> buildOrderSubscribeArgs(List<String> symbols) {
        List<SubscribeArg> args = new ArrayList<>();
        symbols.stream().map(symbol ->
                SubscribeArg.builder().instType("SPOT").channel(ORDER_CHANNEL).instId(symbol).build()).forEach(args::add);
        return args;
    }

    protected void subscribeToAccount(WebSocketSession session) throws IOException {
        List<SubscribeArg> args = new ArrayList<>();
        args.add(buildAccountSubscribeArg());
        SubscribeRequest subscribeRequest = SubscribeRequest.builder().op("subscribe").args(args).build();
        String subscribeJson = JacksonUtil.toJsonStr(subscribeRequest);
        session.sendMessage(new TextMessage(subscribeJson));
    }

    protected void subscribeToOrders(WebSocketSession session, List<String> symbols) throws IOException {
        List<SubscribeArg> args = buildOrderSubscribeArgs(symbols);
        SubscribeRequest subscribeRequest = SubscribeRequest.builder().op("subscribe").args(args).build();
        String subscribeJson = JacksonUtil.toJsonStr(subscribeRequest);
        session.sendMessage(new TextMessage(subscribeJson));
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    // 该方法放到抽象类中是因为有3个handler都需要用到
    protected boolean isOrderFromOrderChannel(JsonNode jsonNode) {
        return jsonNode.has(ARG)
                && jsonNode.has(DATA)
                && jsonNode.get(ARG).has(CHANNEL)
                && ORDER_CHANNEL.equals(jsonNode.get(ARG).get(CHANNEL).asText());
    }
}
