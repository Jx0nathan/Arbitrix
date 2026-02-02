package io.arbitrix.price_moving;

import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import io.arbitrix.core.common.domain.ExchangeOrder;
import io.arbitrix.core.common.enums.OrderSide;
import io.arbitrix.core.strategy.price_moving.PriceTierMoveService;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ClassifyMovingOrderTest {

    PriceTierMoveService priceTierMoveService = new PriceTierMoveService(null, null, null, null);

    @Test
    public void classifyMovingOrderInLowerFlow01() {
        ExchangeOrder order1 = ExchangeOrder.limitMarketSell("BTC-USDT", "1", "35181.94", "3");
        order1.setPriceLevel(3);
        ExchangeOrder order2 = ExchangeOrder.limitMarketSell("BTC-USDT", "1", "35181.93", "2"); // 平移 3
        order2.setPriceLevel(2);
        ExchangeOrder order3 = ExchangeOrder.limitMarketSell("BTC-USDT", "1", "35181.92", "1"); // 平移 2
        order3.setPriceLevel(1);

        ExchangeOrder order4 = ExchangeOrder.limitMarketBuy("BTC-USDT", "1", "35181.90", "1");
        order4.setPriceLevel(1);
        ExchangeOrder order5 = ExchangeOrder.limitMarketBuy("BTC-USDT", "1", "35181.89", "2"); // 平移 1
        order5.setPriceLevel(2);
        ExchangeOrder order6 = ExchangeOrder.limitMarketBuy("BTC-USDT", "1", "35181.88", "3"); // 平移 2
        order6.setPriceLevel(3);

        List<ExchangeOrder> baseOrderList = Arrays.asList(order1, order2, order3, order4, order5, order6);
        List<ExchangeOrder> sortOrderList = baseOrderList.stream().sorted(Comparator.comparing(order -> {
            try {
                return new BigDecimal(order.getPrice());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Price is not a valid integer", e);
            }
        })).collect(Collectors.toList());

        List<ExchangeOrder> exchangeOrderList = priceTierMoveService.classifyMovingOrderInLowerFlow(1, sortOrderList);
        Assertions.assertEquals(4, exchangeOrderList.size());
        exchangeOrderList.forEach((item) -> {
            if ("35181.93".equals(item.getPrice())) {
                Assertions.assertEquals(item.getSide(), OrderSide.SELL);
                Assertions.assertEquals(item.getPriceLevel(), 3);
            }
            if ("35181.92".equals(item.getPrice())) {
                Assertions.assertEquals(item.getSide(), OrderSide.SELL);
                Assertions.assertEquals(item.getPriceLevel(), 2);
            }
            if ("35181.89".equals(item.getPrice())) {
                Assertions.assertEquals(item.getSide(), OrderSide.BUY);
                Assertions.assertEquals(item.getPriceLevel(), 1);
            }
            if ("35181.88".equals(item.getPrice())) {
                Assertions.assertEquals(item.getSide(), OrderSide.BUY);
                Assertions.assertEquals(item.getPriceLevel(), 2);
            }
        });
    }

    @Test
    public void classifyMovingOrderInLowerFlow02() {
        ExchangeOrder order0 = ExchangeOrder.limitMarketSell("BTC-USDT", "1", "35181.95", "4");
        order0.setPriceLevel(4);
        ExchangeOrder order1 = ExchangeOrder.limitMarketSell("BTC-USDT", "1", "35181.94", "3");
        order1.setPriceLevel(3);
        ExchangeOrder order2 = ExchangeOrder.limitMarketSell("BTC-USDT", "1", "35181.93", "2");
        order2.setPriceLevel(2);
        ExchangeOrder order3 = ExchangeOrder.limitMarketSell("BTC-USDT", "1", "35181.92", "1"); // 平移 -> 4
        order3.setPriceLevel(1);

        ExchangeOrder order4 = ExchangeOrder.limitMarketBuy("BTC-USDT", "1", "35181.90", "1");
        order4.setPriceLevel(1);
        ExchangeOrder order5 = ExchangeOrder.limitMarketBuy("BTC-USDT", "1", "35181.89", "2");
        order5.setPriceLevel(2);
        ExchangeOrder order6 = ExchangeOrder.limitMarketBuy("BTC-USDT", "1", "35181.88", "3");
        order6.setPriceLevel(3);
        ExchangeOrder order7 = ExchangeOrder.limitMarketBuy("BTC-USDT", "1", "35181.87", "4"); // 平移 -> 1
        order7.setPriceLevel(4);

        List<ExchangeOrder> baseOrderList = Arrays.asList(order0, order1, order2, order3, order4, order5, order6, order7);
        List<ExchangeOrder> sortOrderList = baseOrderList.stream().sorted(Comparator.comparing(order -> {
            try {
                return new BigDecimal(order.getPrice());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Price is not a valid integer", e);
            }
        })).collect(Collectors.toList());

        List<ExchangeOrder> exchangeOrderList = priceTierMoveService.classifyMovingOrderInLowerFlow(3, sortOrderList);
        Assertions.assertEquals(2, exchangeOrderList.size());
        exchangeOrderList.forEach((item) -> {
            if ("35181.92".equals(item.getPrice())) {
                Assertions.assertEquals(item.getSide(), OrderSide.SELL);
                Assertions.assertEquals(item.getPriceLevel(), 4);
            }
            if ("35181.87".equals(item.getPrice())) {
                Assertions.assertEquals(item.getSide(), OrderSide.BUY);
                Assertions.assertEquals(item.getPriceLevel(), 1);
            }
        });
    }

    @Test
    public void classifyMovingOrderInUpperFlow01() {
        ExchangeOrder order0 = ExchangeOrder.limitMarketSell("BTC-USDT", "1", "66296.14", "4"); // 平移 -> 1
        order0.setPriceLevel(4);
        ExchangeOrder order1 = ExchangeOrder.limitMarketSell("BTC-USDT", "1", "66296.13", "3");
        order1.setPriceLevel(3);
        ExchangeOrder order2 = ExchangeOrder.limitMarketSell("BTC-USDT", "1", "66296.12", "2");
        order2.setPriceLevel(2);
        ExchangeOrder order3 = ExchangeOrder.limitMarketSell("BTC-USDT", "1", "66296.11", "1");
        order3.setPriceLevel(1);

        ExchangeOrder order4 = ExchangeOrder.limitMarketBuy("BTC-USDT", "1", "66296.09", "1"); // 平移 -> 4
        order4.setPriceLevel(1);
        ExchangeOrder order5 = ExchangeOrder.limitMarketBuy("BTC-USDT", "1", "66296.08", "2");
        order5.setPriceLevel(2);
        ExchangeOrder order6 = ExchangeOrder.limitMarketBuy("BTC-USDT", "1", "66296.07", "3");
        order6.setPriceLevel(3);
        ExchangeOrder order7 = ExchangeOrder.limitMarketBuy("BTC-USDT", "1", "66296.06", "4");
        order7.setPriceLevel(4);

        List<ExchangeOrder> baseOrderList = Arrays.asList(order0, order1, order2, order3, order4, order5, order6, order7);
        List<ExchangeOrder> sortOrderList = baseOrderList.stream().sorted(Comparator.comparing(order -> {
            try {
                return new BigDecimal(order.getPrice());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Price is not a valid integer", e);
            }
        })).collect(Collectors.toList());

        List<ExchangeOrder> exchangeOrderList = priceTierMoveService.classifyMovingOrderInUpperFlow(3, sortOrderList);
        Assertions.assertEquals(2, exchangeOrderList.size());
        exchangeOrderList.forEach((item) -> {
            if("66296.09".equals(item.getPrice())){
                Assertions.assertEquals(item.getSide(), OrderSide.BUY);
                Assertions.assertEquals(item.getPriceLevel(), 4);
            }
            if("66296.14".equals(item.getPrice())){
                Assertions.assertEquals(item.getSide(), OrderSide.SELL);
                Assertions.assertEquals(item.getPriceLevel(), 1);
            }
        });
    }

    @Test
    public void classifyMovingOrderInUpperFlow02() {
        ExchangeOrder order0 = ExchangeOrder.limitMarketSell("BTC-USDT", "1", "66296.14", "4"); // 平移 -> 2
        order0.setPriceLevel(4);
        ExchangeOrder order1 = ExchangeOrder.limitMarketSell("BTC-USDT", "1", "66296.13", "3"); // 平移 -> 1
        order1.setPriceLevel(3);
        ExchangeOrder order2 = ExchangeOrder.limitMarketSell("BTC-USDT", "1", "66296.12", "2");
        order2.setPriceLevel(2);
        ExchangeOrder order3 = ExchangeOrder.limitMarketSell("BTC-USDT", "1", "66296.11", "1");
        order3.setPriceLevel(1);

        ExchangeOrder order4 = ExchangeOrder.limitMarketBuy("BTC-USDT", "1", "66296.09", "1"); // 平移 -> 3
        order4.setPriceLevel(1);
        ExchangeOrder order5 = ExchangeOrder.limitMarketBuy("BTC-USDT", "1", "66296.08", "2"); // 平移 -> 4
        order5.setPriceLevel(2);
        ExchangeOrder order6 = ExchangeOrder.limitMarketBuy("BTC-USDT", "1", "66296.07", "3");
        order6.setPriceLevel(3);
        ExchangeOrder order7 = ExchangeOrder.limitMarketBuy("BTC-USDT", "1", "66296.06", "4");
        order7.setPriceLevel(4);

        List<ExchangeOrder> baseOrderList = Arrays.asList(order0, order1, order2, order3, order4, order5, order6, order7);
        List<ExchangeOrder> sortOrderList = baseOrderList.stream().sorted(Comparator.comparing(order -> {
            try {
                return new BigDecimal(order.getPrice());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Price is not a valid integer", e);
            }
        })).collect(Collectors.toList());

        List<ExchangeOrder> exchangeOrderList = priceTierMoveService.classifyMovingOrderInUpperFlow(2, sortOrderList);
        Assertions.assertEquals(4, exchangeOrderList.size());
        exchangeOrderList.forEach((item) -> {
            if("66296.09".equals(item.getPrice())){
                Assertions.assertEquals(item.getSide(), OrderSide.BUY);
                Assertions.assertEquals(item.getPriceLevel(), 3);
            }
            if("66296.08".equals(item.getPrice())){
                Assertions.assertEquals(item.getSide(), OrderSide.BUY);
                Assertions.assertEquals(item.getPriceLevel(), 4);
            }
            if("66296.14".equals(item.getPrice())){
                Assertions.assertEquals(item.getSide(), OrderSide.SELL);
                Assertions.assertEquals(item.getPriceLevel(), 2);
            }
            if("66296.13".equals(item.getPrice())){
                Assertions.assertEquals(item.getSide(), OrderSide.SELL);
                Assertions.assertEquals(item.getPriceLevel(), 1);
            }
        });
    }
}
