package io.arbitrix.core.integration.bybit.wss.enums;

public enum WebSocketOp {
    AUTH("auth"),
    PING("ping"),
    PONG("pong"),
    SUBSCRIBE("subscribe"),
    UNSUBSCRIBE("unsubscribe"),
    ;

    private final String value;

    WebSocketOp(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
