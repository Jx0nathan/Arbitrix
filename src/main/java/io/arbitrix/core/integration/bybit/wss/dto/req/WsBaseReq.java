package io.arbitrix.core.integration.bybit.wss.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.arbitrix.core.integration.bybit.wss.enums.WebSocketOp;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WsBaseReq<T> {

    private String op;

    private List<T> args;
    public static WsBaseReq<String> auth(String apiKey, String timestamp, String sign) {
        WsBaseReq<String> result = new WsBaseReq<>();
        result.setOp(WebSocketOp.AUTH.getValue());
        result.setArgs(List.of(apiKey, timestamp, sign));
        return result;
    }
    public static WsBaseReq ping() {
        WsBaseReq result = new WsBaseReq();
        result.setOp(WebSocketOp.PING.getValue());
        return result;
    }
    public static WsBaseReq<String> subscribe(List<String> topics) {
        WsBaseReq<String> result = new WsBaseReq<>();
        result.setOp(WebSocketOp.SUBSCRIBE.getValue());
        result.setArgs(topics);
        return result;
    }
}