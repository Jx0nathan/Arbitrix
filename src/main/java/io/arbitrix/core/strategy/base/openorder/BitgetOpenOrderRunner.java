package io.arbitrix.core.strategy.base.openorder;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.arbitrix.core.common.domain.Order;
import io.arbitrix.core.common.enums.ExchangeNameEnum;
import io.arbitrix.core.integration.bitget.rest.BitgetRestClient;
import io.arbitrix.core.utils.ExchangeMarketOpenUtilV2;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 针对bitget的订单取消的兜底策略
 *
 */
@Component
@Log4j2
public class BitgetOpenOrderRunner extends ExchangeOpenOrderRunner {
    private final BitgetRestClient bitgetRestClient;

    @Value("${bitget.open.order.expire.time:5000}")
    private Long targetExpireTime;

    public BitgetOpenOrderRunner(ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil, BitgetRestClient bitgetRestClient) {
        super(exchangeMarketOpenUtil, ExchangeNameEnum.BITGET);
        this.bitgetRestClient = bitgetRestClient;
    }

    @Override
    protected List<Order> getOpenOrders() {
        if (symbolPairs.isEmpty()) {
            return Collections.emptyList();
        }
        return bitgetRestClient.getOpenOrders(symbolPairs);
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
        log.info("BitgetOpenOrderRunner.start cancelOrderListStr.size is {} detail is {}", cancelOrderListStr.size(), cancelOrderListStr);
        // TODO 2023/9/27 这里可以考虑调整为批量取消
        for (Order item : cancelOrderList) {
            bitgetRestClient.cancel(item.getSymbol(), item.getClientOrderId());
        }
    }
}
