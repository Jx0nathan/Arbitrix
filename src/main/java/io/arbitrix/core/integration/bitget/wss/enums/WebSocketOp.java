package io.arbitrix.core.integration.bitget.wss.enums;

public enum WebSocketOp {
    LOGIN("login"),
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
