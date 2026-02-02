package io.arbitrix.price_moving;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import io.arbitrix.core.common.domain.ExchangeOrder;
import io.arbitrix.core.common.domain.MovingPriceIndex;
import io.arbitrix.core.common.domain.MovingPriceOrderHolder;
import io.arbitrix.core.common.enums.OrderSide;
import io.arbitrix.core.strategy.price_moving.PriceTierMoveService;
import io.arbitrix.core.common.util.JacksonUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PriceRedefineOrderLevelTest {

    /**
     * ori order list["6","7","8","9","11","12","13","14"]
     * new order list["5","6","7","8","10","11","12","13"]
     */
    @Test
    public void redefineOrderLevelTestOnBuySide1() {
        ExchangeOrder order0 = ExchangeOrder.limitMarketSell("BTC-USDT", "1", "49360.07", "4");
        order0.setPriceLevel(4);
        ExchangeOrder order1 = ExchangeOrder.limitMarketSell("BTC-USDT", "1", "49360.06", "3");
        order1.setPriceLevel(3);
        ExchangeOrder order2 = ExchangeOrder.limitMarketSell("BTC-USDT", "1", "49360.05", "2");
        order2.setPriceLevel(2);
        ExchangeOrder order3 = ExchangeOrder.limitMarketSell("BTC-USDT", "1", "49360.04", "1");
        order3.setPriceLevel(1);

        ExchangeOrder order4 = ExchangeOrder.limitMarketBuy("BTC-USDT", "1", "49360.02", "1");
        order4.setPriceLevel(1);
        ExchangeOrder order5 = ExchangeOrder.limitMarketBuy("BTC-USDT", "1", "49360.01", "2");
        order5.setPriceLevel(2);
        ExchangeOrder order6 = ExchangeOrder.limitMarketBuy("BTC-USDT", "1", "49360.00", "3");
        order6.setPriceLevel(3);
        ExchangeOrder order7 = ExchangeOrder.limitMarketBuy("BTC-USDT", "1", "49359.99", "4");
        order7.setPriceLevel(4);

        List<ExchangeOrder> baseOrderList = Arrays.asList(order0, order1, order2, order3, order4, order5, order6, order7);
        List<ExchangeOrder> sortOrderList = baseOrderList.stream().sorted(Comparator.comparing(order -> {
            try {
                return new BigDecimal(order.getPrice());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Price is not a valid integer", e);
            }
        })).collect(Collectors.toList());

        // 模拟价格9的订单被触发，趋势开始往下 (往下移一位)
        MovingPriceIndex movingPriceIndex = new MovingPriceIndex(1, "49360.04", "49360.02");
        PriceTierMoveService priceTierMoveService = new PriceTierMoveService(null, null, null, null);
        priceTierMoveService.redefineOrderLevel(null, "", movingPriceIndex, OrderSide.BUY, sortOrderList);
        System.out.println(JacksonUtil.toJsonStr(priceTierMoveService.getPriceTierOrderMap()));
    }

    /**
     * ori order list["6","7","8","9","11","12","13","14"]
     * new order list["4","5","6","7","9","10","11","12"]
     */
    @Test
    public void redefineOrderLevelTestOnBuySide2() {
        ExchangeOrder order0 = ExchangeOrder.limitMarketSell("BTC-USDT", "1", "66296.14", "4");
        order0.setPriceLevel(4);
        ExchangeOrder order1 = ExchangeOrder.limitMarketSell("BTC-USDT", "1", "66296.13", "3");
        order1.setPriceLevel(3);
        ExchangeOrder order2 = ExchangeOrder.limitMarketSell("BTC-USDT", "1", "66296.12", "2");
        order2.setPriceLevel(2);
        ExchangeOrder order3 = ExchangeOrder.limitMarketSell("BTC-USDT", "1", "66296.11", "1");
        order3.setPriceLevel(1);

        ExchangeOrder order4 = ExchangeOrder.limitMarketBuy("BTC-USDT", "1", "66296.09", "1");
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

        // 模拟价格9的订单被触发，趋势开始往下 (往下移一位)
        MovingPriceIndex movingPriceIndex = new MovingPriceIndex(3, "66296.11", "66296.09");
        PriceTierMoveService priceTierMoveService = new PriceTierMoveService(null, null, null, null);
        priceTierMoveService.redefineOrderLevel(null, "", movingPriceIndex, OrderSide.SELL, sortOrderList);
        System.out.println(JacksonUtil.toJsonStr(priceTierMoveService.getPriceTierOrderMap()));
    }

    /**
     * ori order list["6","7","8","9","11","12","13","14"]
     * new order list["26","27","28","29","31","32","33","34"]
     */
    @Test
    public void redefineOrderLevelTestOnBuySide3() {
        // 注意：需要mock市场价格为30，这次测试是直接改了代码的返回值
        List<ExchangeOrder> sortBaseOrderList = this.createOriOrder();
        // 模拟价格9的订单被触发，打穿4层订单
        MovingPriceIndex movingPriceIndex = new MovingPriceIndex(0, "11", "9");
        PriceTierMoveService priceTierMoveService = new PriceTierMoveService(null, null, null, null);
        MovingPriceOrderHolder holder = priceTierMoveService.redefineOrderLevel(null, "", movingPriceIndex, OrderSide.BUY, sortBaseOrderList);

        // 卖单全部取消，重新创建买卖订单
        System.out.println("ori order list" + JacksonUtil.toJsonStr(this.getOriFinallyPrice(sortBaseOrderList)));
        System.out.println("new order list" + JacksonUtil.toJsonStr(this.getFinallyPrice(List.of("9", "8", "7", "6"), sortBaseOrderList, holder.getCancelOrderList(), holder.getCreateOrderList())));
    }

    /**
     * ori order list["6","7","8","9","11","12","13","14"]
     * new order list["7","8","9","10","12","13","14","15"]
     */
    @Test
    public void redefineOrderLevelTestOnSellSide1() {
        List<ExchangeOrder> sortBaseOrderList = this.createOriOrder();
        // 模拟价格9的订单被触发，趋势开始往上 (往上移一位)
        MovingPriceIndex movingPriceIndex = new MovingPriceIndex(1, "11", "9");
        PriceTierMoveService priceTierMoveService = new PriceTierMoveService(null, null, null, null);
        MovingPriceOrderHolder holder = priceTierMoveService.redefineOrderLevel(null, "", movingPriceIndex, OrderSide.SELL, sortBaseOrderList);

        // 价格6的取消；创建价格10的买单，价格15的卖单
        System.out.println("ori order list" + JacksonUtil.toJsonStr(this.getOriFinallyPrice(sortBaseOrderList)));
        System.out.println("new order list" + JacksonUtil.toJsonStr(this.getFinallyPrice(List.of("11"), sortBaseOrderList, holder.getCancelOrderList(), holder.getCreateOrderList())));
    }

    /**
     * ori order list["6","7","8","9","11","12","13","14"]
     * new order list["8","9","10","11","13","14","15","16"]
     */
    @Test
    public void redefineOrderLevelTestOnSellSide2() {
        List<ExchangeOrder> sortBaseOrderList = this.createOriOrder();
        // 模拟价格9的订单被触发，趋势开始往上 (往上移一位)
        MovingPriceIndex movingPriceIndex = new MovingPriceIndex(2, "11", "9");
        PriceTierMoveService priceTierMoveService = new PriceTierMoveService(null, null, null, null);
        MovingPriceOrderHolder holder = priceTierMoveService.redefineOrderLevel(null, "", movingPriceIndex, OrderSide.SELL, sortBaseOrderList);

        // 价格6的取消；创建价格10的买单，价格15的卖单
        System.out.println("ori order list" + JacksonUtil.toJsonStr(this.getOriFinallyPrice(sortBaseOrderList)));
        System.out.println("new order list" + JacksonUtil.toJsonStr(this.getFinallyPrice(List.of("11", "12"), sortBaseOrderList, holder.getCancelOrderList(), holder.getCreateOrderList())));
    }

    /**
     * ori order list["6","7","8","9","11","12","13","14"]
     * new order list["26","27","28","29","31","32","33","34"]
     */
    @Test
    public void redefineOrderLevelTestOnSellSide3() {
        List<ExchangeOrder> sortBaseOrderList = this.createOriOrder();
        // 趋势开始往上 价格被打穿，注意：需要mock市场价格为30，这次测试是直接改了代码的返回值
        MovingPriceIndex movingPriceIndex = new MovingPriceIndex(0, "11", "9");
        PriceTierMoveService priceTierMoveService = new PriceTierMoveService(null, null, null, null);
        MovingPriceOrderHolder holder = priceTierMoveService.redefineOrderLevel(null, "", movingPriceIndex, OrderSide.SELL, sortBaseOrderList);

        // 价格6的取消；创建价格10的买单，价格15的卖单
        System.out.println("ori order list" + JacksonUtil.toJsonStr(this.getOriFinallyPrice(sortBaseOrderList)));
        System.out.println("new order list" + JacksonUtil.toJsonStr(this.getFinallyPrice(List.of("11", "12", "13", "14"), sortBaseOrderList, holder.getCancelOrderList(), holder.getCreateOrderList())));
    }

    @Test
    public void redefineOrderLevelTestOnSellSide4() {
        // this.movingPriceLevel = 3;
        // this.tickSize = new BigDecimal("0.01");
        // this.quoteCoinValue = "1000";
        // this.symbol = "BTCUSDT";

        ExchangeOrder order1 = ExchangeOrder.limitMarketSell("BTC-USDT", "1", "35181.94", "3");
        ExchangeOrder order2 = ExchangeOrder.limitMarketSell("BTC-USDT", "1", "35181.93", "2");
        ExchangeOrder order3 = ExchangeOrder.limitMarketSell("BTC-USDT", "1", "35181.92", "1");

        ExchangeOrder order4 = ExchangeOrder.limitMarketBuy("BTC-USDT", "1", "35181.90", "1");
        ExchangeOrder order5 = ExchangeOrder.limitMarketBuy("BTC-USDT", "1", "35181.89", "2");
        ExchangeOrder order6 = ExchangeOrder.limitMarketBuy("BTC-USDT", "1", "35181.88", "3");

        List<ExchangeOrder> baseOrderList = Arrays.asList(order1, order2, order3, order4, order5, order6);
        List<ExchangeOrder> sortOrderList = baseOrderList.stream().sorted(Comparator.comparing(order -> {
            try {
                return new BigDecimal(order.getPrice());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Price is not a valid integer", e);
            }
        })).collect(Collectors.toList());

        MovingPriceIndex movingPriceIndex = new MovingPriceIndex(1, "35181.92", "35181.90");
        PriceTierMoveService priceTierMoveService = new PriceTierMoveService(null, null, null, null);
        MovingPriceOrderHolder holder = priceTierMoveService.redefineOrderLevel(null, "", movingPriceIndex, OrderSide.SELL, sortOrderList);
        Assertions.assertEquals("35181.88", holder.getCancelOrderList().get(0).getPrice());
        Assertions.assertEquals("35181.91", holder.getCreateOrderList().get(0).getPrice());
        Assertions.assertEquals("35181.95", holder.getCreateOrderList().get(1).getPrice());
    }

    @Test
    public void redefineOrderLevelTestOnSellSide5() {
        // this.movingPriceLevel = 3;
        // this.tickSize = new BigDecimal("0.01");
        // this.quoteCoinValue = "1000";
        // this.symbol = "BTCUSDT";

        ExchangeOrder order1 = ExchangeOrder.limitMarketSell("BTC-USDT", "1", "35181.94", "3");
        ExchangeOrder order2 = ExchangeOrder.limitMarketSell("BTC-USDT", "1", "35181.93", "2");
        ExchangeOrder order3 = ExchangeOrder.limitMarketSell("BTC-USDT", "1", "35181.92", "1");

        ExchangeOrder order4 = ExchangeOrder.limitMarketBuy("BTC-USDT", "1", "35181.90", "1");
        ExchangeOrder order5 = ExchangeOrder.limitMarketBuy("BTC-USDT", "1", "35181.89", "2");
        ExchangeOrder order6 = ExchangeOrder.limitMarketBuy("BTC-USDT", "1", "35181.88", "3");

        List<ExchangeOrder> baseOrderList = Arrays.asList(order1, order2, order3, order4, order5, order6);
        List<ExchangeOrder> sortOrderList = baseOrderList.stream().sorted(Comparator.comparing(order -> {
            try {
                return new BigDecimal(order.getPrice());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Price is not a valid integer", e);
            }
        })).collect(Collectors.toList());

        MovingPriceIndex movingPriceIndex = new MovingPriceIndex(2, "35181.92", "35181.90");
        PriceTierMoveService priceTierMoveService = new PriceTierMoveService(null, null, null, null);
        MovingPriceOrderHolder holder = priceTierMoveService.redefineOrderLevel(null, "", movingPriceIndex, OrderSide.SELL, sortOrderList);
        System.out.println(JacksonUtil.toJsonStr(holder.getCancelOrderList()));
        System.out.println(JacksonUtil.toJsonStr(holder.getCreateOrderList()));
        System.out.println(JacksonUtil.toJsonStr(priceTierMoveService.getPriceTierOrderMap()));
    }

    private List<ExchangeOrder> createOriOrder() {
        ExchangeOrder order1 = ExchangeOrder.limitMarketSell("BTC-USDT", "1", "14", "1");
        ExchangeOrder order2 = ExchangeOrder.limitMarketSell("BTC-USDT", "1", "13", "2");
        ExchangeOrder order3 = ExchangeOrder.limitMarketSell("BTC-USDT", "1", "12", "3");
        ExchangeOrder order4 = ExchangeOrder.limitMarketSell("BTC-USDT", "1", "11", "4");
        ExchangeOrder order5 = ExchangeOrder.limitMarketBuy("BTC-USDT", "1", "9", "5");
        ExchangeOrder order6 = ExchangeOrder.limitMarketBuy("BTC-USDT", "1", "8", "6");
        ExchangeOrder order7 = ExchangeOrder.limitMarketBuy("BTC-USDT", "1", "7", "7");
        ExchangeOrder order8 = ExchangeOrder.limitMarketBuy("BTC-USDT", "1", "6", "8");
        List<ExchangeOrder> baseOrderList = Arrays.asList(order1, order2, order3, order4, order5, order6, order7, order8);
        return baseOrderList.stream().sorted(Comparator.comparing(order -> {
            try {
                return new BigDecimal(order.getPrice());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Price is not a valid integer", e);
            }
        })).collect(Collectors.toList());
    }

    private List<String> getOriFinallyPrice(List<ExchangeOrder> sortBaseOrderList) {
        List<String> priceList = new ArrayList<>();
        for (ExchangeOrder order : sortBaseOrderList) {
            priceList.add(order.getPrice());
        }
        return priceList;
    }

    private List<String> getFinallyPrice(List<String> triggerPriceList, List<ExchangeOrder> oriOrderList, List<ExchangeOrder> cancelOrderList, List<ExchangeOrder> createOrderList) {
        List<String> finallyPriceList = new ArrayList<>();
        for (ExchangeOrder order : oriOrderList) {
            finallyPriceList.add(order.getPrice());
        }
        for (ExchangeOrder order : cancelOrderList) {
            finallyPriceList.remove(order.getPrice());
        }
        for (ExchangeOrder order : createOrderList) {
            finallyPriceList.add(order.getPrice());
        }
        for (String str : triggerPriceList) {
            finallyPriceList.remove(str);
        }

        return finallyPriceList.stream().sorted(Comparator.comparing(item -> {
            try {
                return new BigDecimal(item);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Price is not a valid integer", e);
            }
        })).collect(Collectors.toList());
    }
}
