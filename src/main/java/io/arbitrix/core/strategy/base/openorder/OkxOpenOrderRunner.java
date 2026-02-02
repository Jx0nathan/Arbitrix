package io.arbitrix.core.strategy.base.openorder;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.arbitrix.core.common.domain.Order;
import io.arbitrix.core.common.enums.ExchangeNameEnum;
import io.arbitrix.core.integration.okx.rest.OkxCancelOrderClient;
import io.arbitrix.core.integration.okx.rest.OkxOpenOrderClient;
import io.arbitrix.core.utils.ExchangeMarketOpenUtilV2;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 针对okx的订单取消的兜底策略
 *
 * @author jonahan.ji
 */
@Component
@Log4j2
public class OkxOpenOrderRunner extends ExchangeOpenOrderRunner {
    private final OkxOpenOrderClient openOrderClient;
    private final OkxCancelOrderClient okxCancelOrderClient;

    @Value("${okx.open.order.expire.time:5000}")
    private Long targetExpireTime;

    public OkxOpenOrderRunner(ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil, OkxOpenOrderClient openOrderClient, OkxCancelOrderClient okxCancelOrderClient) {
        super(exchangeMarketOpenUtil, ExchangeNameEnum.OKX);
        this.openOrderClient = openOrderClient;
        this.okxCancelOrderClient = okxCancelOrderClient;
    }

    @Override
    protected List<Order> getOpenOrders() {
        if (symbolPairs.isEmpty()) {
            return Collections.emptyList();
        }
        return openOrderClient.getOpenOrders(symbolPairs);
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
        log.info("OkxOpenOrderRunner.start cancelOrderListStr.size is {} detail is {}", cancelOrderListStr.size(), cancelOrderListStr);
        // TODO 2023/9/27 这里可以考虑调整为批量取消
        for (Order item : cancelOrderList) {
            okxCancelOrderClient.cancel(item.getClientOrderId(), item.getSymbol());
        }
    }
}
