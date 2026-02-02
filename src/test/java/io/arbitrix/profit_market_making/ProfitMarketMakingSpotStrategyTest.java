package io.arbitrix.profit_market_making;

import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import io.arbitrix.core.common.domain.OrderExecutionContext;
import io.arbitrix.core.common.domain.ReadyExecuteContext;
import io.arbitrix.core.common.domain.SpotOrderExecutionContext;
import io.arbitrix.core.common.enums.ExchangeNameEnum;
import io.arbitrix.core.common.enums.OrderLevel;
import io.arbitrix.core.common.enums.OrderSide;
import io.arbitrix.core.common.event.BookTickerEvent;
import io.arbitrix.core.common.orderbook.OwnOrderBook;
import io.arbitrix.core.strategy.profit_market_making.order.ProfitMarketMakingSpotStrategy;
import io.arbitrix.core.utils.OrderTradeUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class ProfitMarketMakingSpotStrategyTest {

    private static final BigDecimal BASE_PERCENT = new BigDecimal(1);

    public static void main(String[] args) {
        for (int i = 1; i <= 10; i++) {
            BigDecimal price = new BigDecimal("30000").multiply(BASE_PERCENT.add(BigDecimal.valueOf(i - 1).multiply(new BigDecimal("0.00003"))));
            BigDecimal roundedNumber = price.setScale(2, RoundingMode.DOWN);
            System.out.println(roundedNumber);
        }
    }

    @Test
    public void readyToExecuteTest1() {
        ProfitMarketMakingSpotStrategy fixedSpreadBoostVolume = new ProfitMarketMakingSpotStrategy(null, null, null, null, null, null, null, null, null, null);

        BookTickerEvent bookTickerEvent = new BookTickerEvent();
        SpotOrderExecutionContext context = new SpotOrderExecutionContext();
        context.setExchangeName(ExchangeNameEnum.BYBIT.getValue());
        context.setSymbol("BTCUSDT");
        context.setOrderSide(OrderSide.BUY);
        context.setOrderLevel(OrderLevel.FOURTH_LEVEL.getLevel());
        context.setBookTickerEvent(bookTickerEvent);

        Map<String, OwnOrderBook> ownOrderBook = new HashMap<>();
        context.setOwnOrderBook(ownOrderBook);

        ReadyExecuteContext readyExecuteContext = fixedSpreadBoostVolume.readyToExecute(context);
        Assertions.assertEquals(readyExecuteContext.getCanExecute(), true);
        Assertions.assertNull(readyExecuteContext.getCancelOrderIdList());
        Assertions.assertEquals(readyExecuteContext.getCreateOrderLevel().intValue(), OrderLevel.FOURTH_LEVEL.getLevel());
    }

    @Test
    public void readyToExecuteTest2() {
        ProfitMarketMakingSpotStrategy fixedSpreadBoostVolume = new ProfitMarketMakingSpotStrategy(null, null, null, null, null, null, null, null, null, null);
        BookTickerEvent bookTickerEvent = new BookTickerEvent();
        bookTickerEvent.setBidPrice("100");

        SpotOrderExecutionContext context = new SpotOrderExecutionContext();
        context.setExchangeName(ExchangeNameEnum.BYBIT.getValue());
        context.setSymbol("BTCUSDT");
        context.setOrderSide(OrderSide.BUY);
        context.setOrderLevel(OrderLevel.FOURTH_LEVEL.getLevel());
        context.setBookTickerEvent(bookTickerEvent);

        String orderBookCacheKey = OrderTradeUtil.buildLevelOrderTradeKey(ExchangeNameEnum.BYBIT.getValue(), "BTCUSDT", OrderSide.BUY, OrderLevel.FOURTH_LEVEL.getLevel());
        Map<String, OwnOrderBook> ownOrderBook = new HashMap<>();

        OwnOrderBook orderBook = new OwnOrderBook();
        orderBook.setAnchorPrice("100");
        ownOrderBook.put(orderBookCacheKey, orderBook);
        context.setOwnOrderBook(ownOrderBook);

        ReadyExecuteContext readyExecuteContext = fixedSpreadBoostVolume.readyToExecute(context);
        Assertions.assertEquals(readyExecuteContext.getCanExecute(), true);
        Assertions.assertEquals(readyExecuteContext.getCancelOrderIdList(), null);
        Assertions.assertEquals(readyExecuteContext.getCreateOrderLevel().intValue(), OrderLevel.FOURTH_LEVEL.getLevel());
    }

    @Test
    public void readyToExecuteTest3() {
        ProfitMarketMakingSpotStrategy fixedSpreadBoostVolume = new ProfitMarketMakingSpotStrategy(null, null, null, null, null, null, null, null, null, null);

        BookTickerEvent bookTickerEvent = new BookTickerEvent();
        bookTickerEvent.setBidPrice("100");

        SpotOrderExecutionContext context = new SpotOrderExecutionContext();
        context.setExchangeName(ExchangeNameEnum.BYBIT.getValue());
        context.setSymbol("BTCUSDT");
        context.setOrderSide(OrderSide.BUY);
        context.setOrderLevel(OrderLevel.FOURTH_LEVEL.getLevel());
        context.setBookTickerEvent(bookTickerEvent);

        String orderBookCacheKey = OrderTradeUtil.buildLevelOrderTradeKey(ExchangeNameEnum.BYBIT.getValue(), "BTCUSDT", OrderSide.BUY, OrderLevel.FOURTH_LEVEL.getLevel());
        Map<String, OwnOrderBook> ownOrderBook = new HashMap<>();

        OwnOrderBook orderBook = new OwnOrderBook();
        orderBook.setAnchorPrice("100");
        ownOrderBook.put(orderBookCacheKey, orderBook);
        context.setOwnOrderBook(ownOrderBook);

        ReadyExecuteContext readyExecuteContext = fixedSpreadBoostVolume.readyToExecute(context);
        Assertions.assertEquals(readyExecuteContext.getCanExecute(), true);
        Assertions.assertNull(readyExecuteContext.getCancelOrderIdList());
        Assertions.assertEquals(readyExecuteContext.getCreateOrderLevel().intValue(), OrderLevel.FOURTH_LEVEL.getLevel());
    }

    @Test
    public void calculateBuyPriceOnUsdtPriceTest() {
        BookTickerEvent bctUsdtData = new BookTickerEvent();
        bctUsdtData.setBidPrice("35726.74");

        BookTickerEvent usdcUsdtData = new BookTickerEvent();
        usdcUsdtData.setAskPrice("0.9995");

        ProfitMarketMakingSpotStrategy profitMarketMakingSpotStrategy = new ProfitMarketMakingSpotStrategy(null, null, null, null, null, null, null, null, null, null);
        String price = profitMarketMakingSpotStrategy.calculateBuyPriceOnUsdtPrice(bctUsdtData, usdcUsdtData);
        Assertions.assertEquals(price, "35741.75");
    }

    @Test
    public void calculateSellPriceOnUsdtPriceTest() {
        BookTickerEvent bctUsdtData = new BookTickerEvent();
        bctUsdtData.setAskPrice("35726.75");

        BookTickerEvent usdcUsdtData = new BookTickerEvent();
        usdcUsdtData.setBidPrice("0.9994");

        ProfitMarketMakingSpotStrategy profitMarketMakingSpotStrategy = new ProfitMarketMakingSpotStrategy(null, null, null, null, null, null, null, null, null, null);
        String price = profitMarketMakingSpotStrategy.calculateSellPriceOnUsdtPrice(bctUsdtData, usdcUsdtData);
        Assertions.assertEquals(price, "35751.05");
    }
}
