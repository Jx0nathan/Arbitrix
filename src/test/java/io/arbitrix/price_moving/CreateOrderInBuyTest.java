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

public class CreateOrderInBuyTest {

    PriceTierMoveService priceTierMoveService = new PriceTierMoveService(null, null, null, null);

    @Test
    public void createOrderInBuyTest01() {
        // this.movingPriceLevel = 3;
        // this.tickSize = new BigDecimal("0.01");
        String buy4key = String.format(ORDER_LEVEL_KEY, OrderSide.BUY.getValue(), 3);
        String sell1key = String.format(ORDER_LEVEL_KEY, OrderSide.SELL.getValue(), 1);

        BigDecimal finalPrice = new BigDecimal("69766.90");
        List<ExchangeOrder> exchangeOrderList = priceTierMoveService.classifyCreateOrderInLowerFlow(finalPrice, 1, finalPrice, new BigDecimal("69906.56"), "111");
        List<ExchangeOrder> sortOrderList = exchangeOrderList.stream().sorted(Comparator.comparing(order -> {
            try {
                return new BigDecimal(order.getPrice());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Price is not a valid integer", e);
            }
        })).collect(Collectors.toList());

        Assertions.assertEquals(exchangeOrderList.size(), 2);
        Assertions.assertEquals("69557.62", sortOrderList.get(0).getPrice());
        Assertions.assertEquals("69836.80", sortOrderList.get(1).getPrice());

        Map<String, ExchangeOrder> exchangeOrderMap = priceTierMoveService.getPriceTierOrderMap();
        Assertions.assertEquals("69557.62", exchangeOrderMap.get(buy4key).getPrice());
        Assertions.assertEquals("69836.80", exchangeOrderMap.get(sell1key).getPrice());
    }

    @Test
    public void createOrderInBuyTest02() {
        // this.movingPriceLevel = 3;
        String buy4key = String.format(ORDER_LEVEL_KEY, OrderSide.BUY.getValue(), 3);
        String sell1key = String.format(ORDER_LEVEL_KEY, OrderSide.SELL.getValue(), 1);

        BigDecimal finalPrice = new BigDecimal("69627.24");
        List<ExchangeOrder> exchangeOrderList = priceTierMoveService.classifyCreateOrderInLowerFlow(finalPrice, 1, finalPrice, new BigDecimal("69767.11"), "111");
        List<ExchangeOrder> sortOrderList = exchangeOrderList.stream().sorted(Comparator.comparing(order -> {
            try {
                return new BigDecimal(order.getPrice());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Price is not a valid integer", e);
            }
        })).collect(Collectors.toList());

        System.out.println(sortOrderList.get(0).getPrice());
        System.out.println(sortOrderList.get(1).getPrice());
    }

    @Test
    public void createOrderInBuyTest03() {
        // this.movingPriceLevel = 3;
        // this.tickSize = new BigDecimal("1");
        BigDecimal finalPrice = new BigDecimal("69766.90");
        List<ExchangeOrder> exchangeOrderList = priceTierMoveService.classifyCreateOrderInLowerFlow(finalPrice,2, new BigDecimal("9"), new BigDecimal("11"),"111");
        List<ExchangeOrder> sortOrderList = exchangeOrderList.stream().sorted(Comparator.comparing(order -> {
            try {
                return new BigDecimal(order.getPrice());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Price is not a valid integer", e);
            }
        })).collect(Collectors.toList());

        Assertions.assertEquals(exchangeOrderList.size(), 4);
        Assertions.assertEquals("5", sortOrderList.get(0).getPrice());
        Assertions.assertEquals("6", sortOrderList.get(1).getPrice());
        Assertions.assertEquals("9", sortOrderList.get(2).getPrice());
        Assertions.assertEquals("10", sortOrderList.get(3).getPrice());

        Map<String, ExchangeOrder> exchangeOrderMap = priceTierMoveService.getPriceTierOrderMap();
        Assertions.assertEquals("6", exchangeOrderMap.get(String.format(ORDER_LEVEL_KEY, OrderSide.BUY.getValue(), 2)).getPrice());
        Assertions.assertEquals("5", exchangeOrderMap.get(String.format(ORDER_LEVEL_KEY, OrderSide.BUY.getValue(), 3)).getPrice());
        Assertions.assertEquals("9", exchangeOrderMap.get(String.format(ORDER_LEVEL_KEY, OrderSide.SELL.getValue(), 1)).getPrice());
        Assertions.assertEquals("10", exchangeOrderMap.get(String.format(ORDER_LEVEL_KEY, OrderSide.SELL.getValue(), 2)).getPrice());
    }

}
