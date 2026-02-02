package io.arbitrix.core.strategy.base.openorder;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.arbitrix.core.common.domain.Order;
import io.arbitrix.core.common.enums.ExchangeNameEnum;
import io.arbitrix.core.integration.bybit.config.BybitProperties;
import io.arbitrix.core.integration.bybit.rest.BybitRestClient;
import io.arbitrix.core.integration.bybit.rest.enums.Category;
import io.arbitrix.core.utils.ExchangeMarketOpenUtilV2;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 针对bybit的订单取消的兜底策略
 * @author Charles Meng
 */
@Component
@Log4j2
public class BybitOpenOrderRunner extends ExchangeOpenOrderRunner {
    private final BybitRestClient bybitRestClient;
    private final BybitProperties bybitProperties;

    @Value("${bybit.open.order.expire.time:5000}")
    private Long targetExpireTime;

    public BybitOpenOrderRunner(ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil, BybitRestClient bybitRestClient, BybitProperties bybitProperties) {
        super(exchangeMarketOpenUtil, ExchangeNameEnum.BYBIT);
        if (StringUtils.isEmpty(bybitProperties.getCategory())) {
            throw new RuntimeException("category is null,please check bybit.properties");
        }
        this.bybitProperties = bybitProperties;
        this.bybitRestClient = bybitRestClient;
    }

    @Override
    protected List<Order> getOpenOrders() {
        if (symbolPairs.isEmpty()) {
            return Collections.emptyList();
        }
        Category category = Category.getByCode(bybitProperties.getCategory());
        if (category == null) {
            throw new RuntimeException("category is null,please check bybit.properties");
        }
        return bybitRestClient.getOpenOrders(symbolPairs, category.getCode());
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
        log.info("BybitOpenOrderRunner.start cancelOrderListStr.size is {} detail is {}", cancelOrderListStr.size(), cancelOrderListStr);
        // 2023-09-27: 暂不支持批量取消
        for (Order item : cancelOrderList) {
            bybitRestClient.cancel(item.getSymbol(), bybitProperties.getCategory(), item.getClientOrderId());
        }
    }
}
