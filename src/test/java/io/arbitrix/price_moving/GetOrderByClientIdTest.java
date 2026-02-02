package io.arbitrix.price_moving;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import io.arbitrix.core.common.domain.ExchangeOrder;
import io.arbitrix.core.common.domain.MovingPriceIndex;
import io.arbitrix.core.common.enums.OrderSide;
import io.arbitrix.core.strategy.price_moving.PriceTierMoveService;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.arbitrix.core.strategy.price_moving.PriceTierMoveService.ORDER_LEVEL_KEY;

public class GetOrderByClientIdTest {

    PriceTierMoveService priceTierMoveService = new PriceTierMoveService(null, null, null, null);

    private static final Map<String, ExchangeOrder> orderMap = new ConcurrentHashMap<>();

    @Test
    public void test() {

        ExchangeOrder order0 = ExchangeOrder.limitMarketSell("BTC-USDT", "1", "66296.16", "4-SEll");
        order0.setPriceLevel(4);
        orderMap.put(String.format(ORDER_LEVEL_KEY, OrderSide.SELL.getValue(), 4), order0);

        ExchangeOrder order1 = ExchangeOrder.limitMarketSell("BTC-USDT", "1", "66296.15", "3-SEll");
        order1.setPriceLevel(3);
        orderMap.put(String.format(ORDER_LEVEL_KEY, OrderSide.SELL.getValue(), 3), order1);

        ExchangeOrder order3 = ExchangeOrder.limitMarketSell("BTC-USDT", "1", "66296.14", "2-SEll");
        order3.setPriceLevel(2);
        orderMap.put(String.format(ORDER_LEVEL_KEY, OrderSide.SELL.getValue(), 2), order3);

        ExchangeOrder order4 = ExchangeOrder.limitMarketSell("BTC-USDT", "1", "66296.13", "1-SEll");
        order4.setPriceLevel(1);
        orderMap.put(String.format(ORDER_LEVEL_KEY, OrderSide.SELL.getValue(), 1), order4);

        ExchangeOrder order5 = ExchangeOrder.limitMarketBuy("BTC-USDT", "1", "66296.11", "1-BUY");
        order5.setPriceLevel(1);
        orderMap.put(String.format(ORDER_LEVEL_KEY, OrderSide.BUY.getValue(), 1), order5);

        ExchangeOrder order6 = ExchangeOrder.limitMarketBuy("BTC-USDT", "1", "66296.10", "2-BUY");
        order6.setPriceLevel(2);
        orderMap.put(String.format(ORDER_LEVEL_KEY, OrderSide.BUY.getValue(), 2), order6);

        ExchangeOrder order7 = ExchangeOrder.limitMarketBuy("BTC-USDT", "1", "66296.09", "3-BUY");
        order7.setPriceLevel(3);
        orderMap.put(String.format(ORDER_LEVEL_KEY, OrderSide.BUY.getValue(), 3), order7);

        ExchangeOrder order8 = ExchangeOrder.limitMarketBuy("BTC-USDT", "1", "66296.08", "4-BUY");
        order8.setPriceLevel(4);
        orderMap.put(String.format(ORDER_LEVEL_KEY, OrderSide.BUY.getValue(), 4), order8);


        this.getOrderByClientId("1-BUY");

    }

    public Pair<ExchangeOrder, MovingPriceIndex> getOrderByClientId(String clientId) {
        MovingPriceIndex movingPriceIndex = new MovingPriceIndex();
        ExchangeOrder exchangeOrder = null;

        // 获取买单最低价和卖单最高价
        String minBuyKey = String.format(ORDER_LEVEL_KEY, OrderSide.BUY.getValue(), 4);
        String maxSellKey = String.format(ORDER_LEVEL_KEY, OrderSide.SELL.getValue(), 4);

        BigDecimal minBuyPrice = new BigDecimal(orderMap.get(minBuyKey).getPrice());
        BigDecimal maxSellPrice = new BigDecimal(orderMap.get(maxSellKey).getPrice());

        for (Map.Entry<String, ExchangeOrder> entry : orderMap.entrySet()) {
            exchangeOrder = entry.getValue();
            if (clientId.equals(exchangeOrder.getNewClientOrderId())) {
                // 如果被成交的价格命中了最低买单价或者最高卖单价，意味着可能出现了订单价格穿透了自己的挂单，需要重新计算
                if (new BigDecimal(entry.getValue().getPrice()).compareTo(minBuyPrice) == 0
                        || new BigDecimal(entry.getValue().getPrice()).compareTo(maxSellPrice) == 0) {
                    movingPriceIndex.setLevel(0);
                } else {
                    movingPriceIndex.setLevel(exchangeOrder.getPriceLevel());
                }
                break;
            }
        }

        String firstBuyKey = String.format(ORDER_LEVEL_KEY, OrderSide.BUY.getValue(), 1);
        ExchangeOrder firstBuyOrder = orderMap.get(firstBuyKey);
        movingPriceIndex.setBid1Price(firstBuyOrder.getPrice());

        String firstSellKey = String.format(ORDER_LEVEL_KEY, OrderSide.SELL.getValue(), 1);
        ExchangeOrder firstSellOrder = orderMap.get(firstSellKey);
        movingPriceIndex.setAsk1Price(firstSellOrder.getPrice());

        return Pair.of(exchangeOrder, movingPriceIndex);
    }
}
