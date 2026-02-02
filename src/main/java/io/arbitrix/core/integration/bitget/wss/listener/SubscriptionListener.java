package io.arbitrix.core.integration.bitget.wss.listener;

@FunctionalInterface
public interface SubscriptionListener {
    void onReceive(String data);
}
