package io.arbitrix.core.integration.okx.utils;

import org.springframework.web.socket.WebSocketMessage;

public class OkxWSSUtil {

    public static boolean isPongMessage(WebSocketMessage<?> message) {
        if ("pong".equals(message.getPayload())) {
            return true;
        }
        return false;
    }
}
