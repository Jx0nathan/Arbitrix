package io.arbitrix.core.integration.binance.wss;

@FunctionalInterface
public interface OrderBookDepthDataWithSymbolCallback{
    void onReceive(String symbol,String data);
}
