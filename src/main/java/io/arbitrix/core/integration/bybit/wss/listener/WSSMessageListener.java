package io.arbitrix.core.integration.bybit.wss.listener;

/**
 * @author mcx
 * @date 2023/9/27
 * @description
 */
@FunctionalInterface
public interface WSSMessageListener {
    void onReceive(String msg);
}
