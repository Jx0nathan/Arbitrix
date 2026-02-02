package io.arbitrix.core.integration.bybit.wss;

import lombok.Getter;
import io.arbitrix.core.common.enums.WSStreamType;
import io.arbitrix.core.integration.bybit.config.BybitProperties;
import io.arbitrix.core.integration.bybit.wss.listener.WSSMessageListener;

/**
 * @author mcx
 * @date 2023/9/20
 * @description
 */
@Getter
public class BybitConnectionBuilder {
    private WSSMessageListener listener;
    private WSSMessageListener errorListener;
    private BybitProperties bybitProperties;
    private String baseUrl;
    private boolean needAuth;
    private WSStreamType wsStreamType;

    public BybitConnectionBuilder listener(WSSMessageListener listener) {
        this.listener = listener;
        return this;
    }

    public BybitConnectionBuilder errorListener(WSSMessageListener errorListener) {
        this.errorListener = errorListener;
        return this;
    }

    public BybitConnectionBuilder properties(BybitProperties bybitProperties) {
        this.bybitProperties = bybitProperties;
        return this;
    }

    public BybitConnectionBuilder baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public BybitConnectionBuilder needAuth(boolean needAuth) {
        this.needAuth = needAuth;
        return this;
    }

    public BybitConnectionBuilder wsStreamType(WSStreamType wsStreamType) {
        this.wsStreamType = wsStreamType;
        return this;
    }

    public BybitWebSocketConnection build() {
        return new BybitWebSocketConnection(bybitProperties, baseUrl, needAuth, listener, errorListener, wsStreamType);
    }

}
