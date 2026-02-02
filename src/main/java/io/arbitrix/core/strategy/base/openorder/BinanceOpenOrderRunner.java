package io.arbitrix.core.strategy.base.openorder;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.arbitrix.core.common.domain.Order;
import io.arbitrix.core.common.enums.ExchangeNameEnum;
import io.arbitrix.core.integration.binance.rest.BinanceClient;
import io.arbitrix.core.integration.binance.wss.BinanceWebSocketClient;
import io.arbitrix.core.utils.ExchangeMarketOpenUtilV2;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 针对binance的订单取消的兜底策略
 *
 * @author jonahan.ji
 */
@Component
@Log4j2
public class BinanceOpenOrderRunner extends ExchangeOpenOrderRunner {
    private final BinanceClient binanceClient;
    private final BinanceWebSocketClient binanceWebSocketApiClient;

    @Value("${binance.open.order.expire.time:5000}")
    private Long targetExpireTime;

    public BinanceOpenOrderRunner(ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil, BinanceClient binanceClient, BinanceWebSocketClient binanceWebSocketApiClient) {
        super(exchangeMarketOpenUtil, ExchangeNameEnum.BINANCE);
        this.binanceClient = binanceClient;
        this.binanceWebSocketApiClient = binanceWebSocketApiClient;
    }

    @Override
    protected List<Order> getOpenOrders() {
        if (symbolPairs.isEmpty()) {
            return Collections.emptyList();
        }
        return binanceClient.getOpenOrders(symbolPairs);
    }

    @Override
    public long getExpireTime() {
        return targetExpireTime;
    }


    @Override
    protected void cancelOpenOrders(List<Order> cancelOrderList) {
        if (cancelOrderList.isEmpty()) {
            return;
        }
        List<String> cancelOrderListStr = cancelOrderList.stream().map(item -> item.getSymbol() + " " + item.getClientOrderId()).collect(Collectors.toList());
        log.info("BinanceOpenOrderRunner.start cancelOrderListStr.size is {} detail is {}", cancelOrderListStr.size(), cancelOrderListStr);
        // TODO 2023/9/27 这里可以考虑调整为批量取消
        for (Order item : cancelOrderList) {
            binanceWebSocketApiClient.cancelOrder(item.getSymbol(), item.getClientOrderId());
        }
    }
}
