package io.arbitrix.price_moving;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import io.arbitrix.core.common.domain.ExchangeOrder;
import io.arbitrix.core.common.domain.MovingPriceIndex;
import io.arbitrix.core.common.enums.OrderSide;
import io.arbitrix.core.strategy.price_moving.PriceTierMoveService;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CalculatePriceLevelTest {

    /**
     * 注意这个测试的层级是5，需要修改movingPriceLevel
     */
    @Test
    public void calculatePriceLevelTest01() {
        PriceTierMoveService priceTierMoveService = new PriceTierMoveService(null,null, null, null);

        List<ExchangeOrder> exchangeOrderList = this.createOriOrder();
        MovingPriceIndex movingPriceIndex1 = priceTierMoveService.calculatePriceLevel(exchangeOrderList, OrderSide.BUY, "9");
        Assertions.assertEquals(movingPriceIndex1.getAsk1Price(), "11");
        Assertions.assertEquals(movingPriceIndex1.getBid1Price(), "9");
        Assertions.assertEquals(movingPriceIndex1.getLevel(), 1);

        MovingPriceIndex movingPriceIndex2 = priceTierMoveService.calculatePriceLevel(exchangeOrderList, OrderSide.BUY, "8");
        Assertions.assertEquals(movingPriceIndex2.getAsk1Price(), "11");
        Assertions.assertEquals(movingPriceIndex2.getBid1Price(), "9");
        Assertions.assertEquals(movingPriceIndex2.getLevel(), 2);

        MovingPriceIndex movingPriceIndex3 = priceTierMoveService.calculatePriceLevel(exchangeOrderList, OrderSide.BUY, "7");
        Assertions.assertEquals(movingPriceIndex3.getAsk1Price(), "11");
        Assertions.assertEquals(movingPriceIndex3.getBid1Price(), "9");
        Assertions.assertEquals(movingPriceIndex3.getLevel(), 3);

        MovingPriceIndex movingPriceIndex4 = priceTierMoveService.calculatePriceLevel(exchangeOrderList, OrderSide.BUY, "6");
        Assertions.assertEquals(movingPriceIndex4.getAsk1Price(), "11");
        Assertions.assertEquals(movingPriceIndex4.getBid1Price(), "9");
        Assertions.assertEquals(movingPriceIndex4.getLevel(), 4);

        MovingPriceIndex movingPriceIndex5 = priceTierMoveService.calculatePriceLevel(exchangeOrderList, OrderSide.SELL, "11");
        Assertions.assertEquals(movingPriceIndex5.getAsk1Price(), "11");
        Assertions.assertEquals(movingPriceIndex5.getBid1Price(), "9");
        Assertions.assertEquals(movingPriceIndex5.getLevel(), 1);

        MovingPriceIndex movingPriceIndex6 = priceTierMoveService.calculatePriceLevel(exchangeOrderList, OrderSide.SELL, "12");
        Assertions.assertEquals(movingPriceIndex6.getAsk1Price(), "11");
        Assertions.assertEquals(movingPriceIndex6.getBid1Price(), "9");
        Assertions.assertEquals(movingPriceIndex6.getLevel(), 2);

        MovingPriceIndex movingPriceIndex7 = priceTierMoveService.calculatePriceLevel(exchangeOrderList, OrderSide.SELL, "13");
        Assertions.assertEquals(movingPriceIndex7.getAsk1Price(), "11");
        Assertions.assertEquals(movingPriceIndex7.getBid1Price(), "9");
        Assertions.assertEquals(movingPriceIndex7.getLevel(), 3);

        MovingPriceIndex movingPriceIndex8 = priceTierMoveService.calculatePriceLevel(exchangeOrderList, OrderSide.SELL, "14");
        Assertions.assertEquals(movingPriceIndex8.getAsk1Price(), "11");
        Assertions.assertEquals(movingPriceIndex8.getBid1Price(), "9");
        Assertions.assertEquals(movingPriceIndex8.getLevel(), 4);

        MovingPriceIndex movingPriceIndex9 = priceTierMoveService.calculatePriceLevel(exchangeOrderList, OrderSide.SELL, "15");
        Assertions.assertEquals(movingPriceIndex9.getAsk1Price(), "11");
        Assertions.assertEquals(movingPriceIndex9.getBid1Price(), "9");
        Assertions.assertEquals(movingPriceIndex9.getLevel(), 0);

        MovingPriceIndex movingPriceIndex10 = priceTierMoveService.calculatePriceLevel(exchangeOrderList, OrderSide.BUY, "5");
        Assertions.assertEquals(movingPriceIndex10.getAsk1Price(), "11");
        Assertions.assertEquals(movingPriceIndex10.getBid1Price(), "9");
        Assertions.assertEquals(movingPriceIndex10.getLevel(), 0);

        MovingPriceIndex movingPriceIndex11 = priceTierMoveService.calculatePriceLevel(exchangeOrderList, OrderSide.SELL, "25");
        Assertions.assertEquals(movingPriceIndex11.getLevel(), 0);

        MovingPriceIndex movingPriceIndex12 = priceTierMoveService.calculatePriceLevel(exchangeOrderList, OrderSide.BUY, "1");
        Assertions.assertEquals(movingPriceIndex12.getLevel(), 0);
    }

    private List<ExchangeOrder> createOriOrder() {
        ExchangeOrder order0 = ExchangeOrder.limitMarketSell("BTC-USDT", "1", "15", "0");
        ExchangeOrder order1 = ExchangeOrder.limitMarketSell("BTC-USDT", "1", "14", "1");
        ExchangeOrder order2 = ExchangeOrder.limitMarketSell("BTC-USDT", "1", "13", "2");
        ExchangeOrder order3 = ExchangeOrder.limitMarketSell("BTC-USDT", "1", "12", "3");
        ExchangeOrder order4 = ExchangeOrder.limitMarketSell("BTC-USDT", "1", "11", "4");
        ExchangeOrder order5 = ExchangeOrder.limitMarketBuy("BTC-USDT", "1", "9", "5");
        ExchangeOrder order6 = ExchangeOrder.limitMarketBuy("BTC-USDT", "1", "8", "6");
        ExchangeOrder order7 = ExchangeOrder.limitMarketBuy("BTC-USDT", "1", "7", "7");
        ExchangeOrder order8 = ExchangeOrder.limitMarketBuy("BTC-USDT", "1", "6", "8");
        ExchangeOrder order9 = ExchangeOrder.limitMarketBuy("BTC-USDT", "1", "5", "9");
        List<ExchangeOrder> baseOrderList = Arrays.asList(order0, order1, order2, order3, order4, order5, order6, order7, order8, order9);
        return baseOrderList.stream().sorted(Comparator.comparing(order -> {
            try {
                return new BigDecimal(order.getPrice());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Price is not a valid integer", e);
            }
        })).collect(Collectors.toList());
    }
}
