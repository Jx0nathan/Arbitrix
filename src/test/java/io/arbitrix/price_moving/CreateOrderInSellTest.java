package io.arbitrix.price_moving;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import io.arbitrix.core.common.domain.ExchangeOrder;
import io.arbitrix.core.common.enums.OrderSide;
import io.arbitrix.core.strategy.price_moving.PriceTierMoveService;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.arbitrix.core.strategy.price_moving.PriceTierMoveService.ORDER_LEVEL_KEY;

public class CreateOrderInSellTest {

    PriceTierMoveService priceTierMoveService = new PriceTierMoveService(null, null, null, null);

    @Test
    public void createOrderInSellTest01() {
        // this.movingPriceLevel = 3;
        // this.tickSize = new BigDecimal("0.01");
        // this.quoteCoinValue = "1000";
        // this.symbol = "BTCUSDT";
        String buy1key = String.format(ORDER_LEVEL_KEY, OrderSide.BUY.getValue(), 1);
        String buy2key = String.format(ORDER_LEVEL_KEY, OrderSide.BUY.getValue(), 2);
        String sell2key = String.format(ORDER_LEVEL_KEY, OrderSide.SELL.getValue(), 2);
        String sell3key = String.format(ORDER_LEVEL_KEY, OrderSide.SELL.getValue(), 3);

        List<ExchangeOrder> exchangeOrderList = priceTierMoveService.classifyCreateOrderInUpperFlow(null, 2, new BigDecimal("35181.90"), new BigDecimal("35181.92"), "");
        List<ExchangeOrder> sortOrderList = exchangeOrderList.stream().sorted(Comparator.comparing(order -> {
            try {
                return new BigDecimal(order.getPrice());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Price is not a valid integer", e);
            }
        })).collect(Collectors.toList());

        Assertions.assertEquals("35181.91", sortOrderList.get(0).getPrice());
        Assertions.assertEquals("35181.92", sortOrderList.get(1).getPrice());
        Assertions.assertEquals("35181.95", sortOrderList.get(2).getPrice());
        Assertions.assertEquals("35181.96", sortOrderList.get(3).getPrice());

        Map<String, ExchangeOrder> exchangeOrderMap = priceTierMoveService.getPriceTierOrderMap();
        Assertions.assertEquals("35181.91", exchangeOrderMap.get(buy2key).getPrice());
        Assertions.assertEquals("35181.92", exchangeOrderMap.get(buy1key).getPrice());
        Assertions.assertEquals("35181.95", exchangeOrderMap.get(sell2key).getPrice());
        Assertions.assertEquals("35181.96", exchangeOrderMap.get(sell3key).getPrice());
    }

    @Test
    public void createOrderInSellTest02() {
        // this.movingPriceLevel = 4;
        // this.tickSize = new BigDecimal("0.01");
        // this.quoteCoinValue = "1000";
        // this.symbol = "BTCUSDT";
        String buy1key = String.format(ORDER_LEVEL_KEY, OrderSide.BUY.getValue(), 1);
        String sell4key = String.format(ORDER_LEVEL_KEY, OrderSide.SELL.getValue(), 4);

        List<ExchangeOrder> exchangeOrderList = priceTierMoveService.classifyCreateOrderInUpperFlow(null, 1, new BigDecimal("9"), new BigDecimal("11"), "");
        List<ExchangeOrder> sortOrderList = exchangeOrderList.stream().sorted(Comparator.comparing(order -> {
            try {
                return new BigDecimal(order.getPrice());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Price is not a valid integer", e);
            }
        })).collect(Collectors.toList());

        Assertions.assertEquals("10", sortOrderList.get(0).getPrice());
        Assertions.assertEquals("15", sortOrderList.get(1).getPrice());

        Map<String, ExchangeOrder> exchangeOrderMap = priceTierMoveService.getPriceTierOrderMap();
        Assertions.assertEquals("10", exchangeOrderMap.get(buy1key).getPrice());
        Assertions.assertEquals("15", exchangeOrderMap.get(sell4key).getPrice());
    }

    @Test
    public void createOrderInSellTest03() {
        // this.movingPriceLevel = 10;
        // this.tickSize = new BigDecimal("0.01");
        List<ExchangeOrder> exchangeOrderList = priceTierMoveService.classifyCreateOrderInUpperFlow(null, 6, new BigDecimal("9"), new BigDecimal("11"), "");
        List<ExchangeOrder> sortOrderList = exchangeOrderList.stream().sorted(Comparator.comparing(order -> {
            try {
                return new BigDecimal(order.getPrice());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Price is not a valid integer", e);
            }
        })).collect(Collectors.toList());

        Assertions.assertEquals(12, sortOrderList.size());

        Assertions.assertEquals("9.01", sortOrderList.get(0).getPrice());
        Assertions.assertEquals("9.02", sortOrderList.get(1).getPrice());
        Assertions.assertEquals("9.03", sortOrderList.get(2).getPrice());
        Assertions.assertEquals("9.04", sortOrderList.get(3).getPrice());
        Assertions.assertEquals("9.05", sortOrderList.get(4).getPrice());
        Assertions.assertEquals("9.06", sortOrderList.get(5).getPrice());
        Assertions.assertEquals("11.10", sortOrderList.get(6).getPrice());
        Assertions.assertEquals("11.11", sortOrderList.get(7).getPrice());
        Assertions.assertEquals("11.12", sortOrderList.get(8).getPrice());
        Assertions.assertEquals("11.13", sortOrderList.get(9).getPrice());
        Assertions.assertEquals("11.14", sortOrderList.get(10).getPrice());
        Assertions.assertEquals("11.15", sortOrderList.get(11).getPrice());

        Map<String, ExchangeOrder> exchangeOrderMap = priceTierMoveService.getPriceTierOrderMap();
        Assertions.assertEquals("9.06", exchangeOrderMap.get(String.format(ORDER_LEVEL_KEY, OrderSide.BUY.getValue(), 1)).getPrice());
        Assertions.assertEquals("9.05", exchangeOrderMap.get(String.format(ORDER_LEVEL_KEY, OrderSide.BUY.getValue(), 2)).getPrice());
        Assertions.assertEquals("9.04", exchangeOrderMap.get(String.format(ORDER_LEVEL_KEY, OrderSide.BUY.getValue(), 3)).getPrice());
        Assertions.assertEquals("9.03", exchangeOrderMap.get(String.format(ORDER_LEVEL_KEY, OrderSide.BUY.getValue(), 4)).getPrice());
        Assertions.assertEquals("9.02", exchangeOrderMap.get(String.format(ORDER_LEVEL_KEY, OrderSide.BUY.getValue(), 5)).getPrice());
        Assertions.assertEquals("9.01", exchangeOrderMap.get(String.format(ORDER_LEVEL_KEY, OrderSide.BUY.getValue(), 6)).getPrice());

        Assertions.assertEquals("11.10", exchangeOrderMap.get(String.format(ORDER_LEVEL_KEY, OrderSide.SELL.getValue(), 5)).getPrice());
        Assertions.assertEquals("11.11", exchangeOrderMap.get(String.format(ORDER_LEVEL_KEY, OrderSide.SELL.getValue(), 6)).getPrice());
        Assertions.assertEquals("11.12", exchangeOrderMap.get(String.format(ORDER_LEVEL_KEY, OrderSide.SELL.getValue(), 7)).getPrice());
        Assertions.assertEquals("11.13", exchangeOrderMap.get(String.format(ORDER_LEVEL_KEY, OrderSide.SELL.getValue(), 8)).getPrice());
        Assertions.assertEquals("11.14", exchangeOrderMap.get(String.format(ORDER_LEVEL_KEY, OrderSide.SELL.getValue(), 9)).getPrice());
        Assertions.assertEquals("11.15", exchangeOrderMap.get(String.format(ORDER_LEVEL_KEY, OrderSide.SELL.getValue(), 10)).getPrice());
    }
}
