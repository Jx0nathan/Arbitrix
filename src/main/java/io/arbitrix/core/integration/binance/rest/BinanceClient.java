package io.arbitrix.core.integration.binance.rest;

import com.binance.connector.client.impl.SpotClientImpl;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import io.arbitrix.core.common.domain.ExchangeOrder;
import io.arbitrix.core.common.domain.Order;
import io.arbitrix.core.common.enums.OrderSide;
import io.arbitrix.core.common.enums.OrderType;
import io.arbitrix.core.common.monitor.annotation.PercentilesMetrics;
import io.arbitrix.core.common.util.JacksonUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author jonathan.ji
 */
@Component
@Log4j2
public class BinanceClient {
    private final SpotClientImpl spotClient;

    public BinanceClient(SpotClientImpl spotClient) {
        this.spotClient = spotClient;
    }

    public void newOrderBatch(ExchangeOrder sportOrder) {
        this.newOrder(sportOrder.getSymbol(), sportOrder.getSide(), sportOrder.getType(), sportOrder.getQuantity(), sportOrder.getPrice(), sportOrder.getNewClientOrderId());
    }

    public void newOrder(String symbol, OrderSide orderSide, OrderType type, String quantity, String price, String clientOrderId) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", symbol);
        parameters.put("side", orderSide.name());
        parameters.put("type", type.name());
        parameters.put("price", price);
        parameters.put("quantity", quantity);
        parameters.put("newClientOrderId", clientOrderId);
        parameters.put("newOrderRespType", "RESULT");

        try {
            String result = spotClient.createTrade().newOrder(parameters);
            log.info("BinanceClient.newOrder.result: {}", result);
        } catch (Exception ex) {
            log.error("BinanceClient.newOrder error", ex);
        }
    }
    @PercentilesMetrics
    public List<Order> getOpenOrders(List<String> symbols){
        List<Order> orderList = new ArrayList<>();
        for (String symbol : symbols) {
            try {
                LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
                parameters.put("symbol", symbol);
                String orderStr = spotClient.createTrade().getOpenOrders(parameters);
                orderList.addAll(JacksonUtil.fromList(orderStr, Order.class));
            } catch (Exception e) {
                log.error("BinanceClient.getOpenOrders symbol {} error", symbol, e);
            }
        }
        return orderList;
    }
}