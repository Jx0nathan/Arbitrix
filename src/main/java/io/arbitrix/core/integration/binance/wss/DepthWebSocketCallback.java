package io.arbitrix.core.integration.binance.wss;

import com.binance.connector.client.utils.WebSocketCallback;
import lombok.Data;


@Data
public class DepthWebSocketCallback implements WebSocketCallback {
    private final String symbol;
    private final OrderBookDepthDataWithSymbolCallback orderBookDepthDataWithSymbolCallback;

    public DepthWebSocketCallback(String symbol, OrderBookDepthDataWithSymbolCallback orderBookDepthDataWithSymbolCallback) {
        this.symbol = symbol;
        this.orderBookDepthDataWithSymbolCallback = orderBookDepthDataWithSymbolCallback;
    }

    @Override
    public void onReceive(String data) {
        this.orderBookDepthDataWithSymbolCallback.onReceive(symbol, data);
    }
}
