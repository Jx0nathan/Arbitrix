package io.arbitrix.core.integration.bitget.wss;

import lombok.Getter;
import io.arbitrix.core.integration.bitget.wss.listener.SubscriptionListener;

/**
 * @author mcx
 * @date 2023/9/20
 * @description
 */
@Getter
public class BitgetConnectionBuilder {
    private String pushUrl;
    private boolean isLogin;
    private String apiKey;
    private String secretKey;
    private String passPhrase;
    private SubscriptionListener listener;
    private SubscriptionListener errorListener;

    public BitgetConnectionBuilder listener(SubscriptionListener listener) {
        this.listener = listener;
        return this;
    }

    public BitgetConnectionBuilder errorListener(SubscriptionListener errorListener) {
        this.errorListener = errorListener;
        return this;
    }

    public BitgetConnectionBuilder pushUrl(String pushUrl) {
        this.pushUrl = pushUrl;
        return this;
    }

    public BitgetConnectionBuilder isLogin(boolean isLogin) {
        this.isLogin = isLogin;
        return this;
    }

    public BitgetConnectionBuilder apiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    public BitgetConnectionBuilder secretKey(String secretKey) {
        this.secretKey = secretKey;
        return this;
    }

    public BitgetConnectionBuilder passPhrase(String passPhrase) {
        this.passPhrase = passPhrase;
        return this;
    }

    public BitGetWebSocketConnection build() {
        return new BitGetWebSocketConnection(this);
    }
}
