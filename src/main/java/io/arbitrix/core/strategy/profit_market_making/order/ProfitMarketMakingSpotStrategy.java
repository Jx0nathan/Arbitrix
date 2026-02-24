package io.arbitrix.core.strategy.profit_market_making.order;

import io.arbitrix.core.common.util.EnvUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.arbitrix.core.common.domain.ExchangeOrder;
import io.arbitrix.core.common.domain.ReadyExecuteContext;
import io.arbitrix.core.common.domain.SpotOrderExecutionContext;
import io.arbitrix.core.common.enums.OrderSide;
import io.arbitrix.core.common.event.BookTickerEvent;
import io.arbitrix.core.common.event.OrderTradeUpdateEvent;
import io.arbitrix.core.common.monitor.StrategyMonitor;
import io.arbitrix.core.common.orderbook.OwnOrderBook;
import io.arbitrix.core.facade.MarketFacade;
import io.arbitrix.core.integration.binance.wss.BinanceWebSocketClient;
import io.arbitrix.core.integration.bitget.rest.BitgetRestClient;
import io.arbitrix.core.integration.bybit.rest.BybitRestClient;
import io.arbitrix.core.integration.bybit.rest.enums.Category;
import io.arbitrix.core.integration.okx.rest.OkxCancelOrderClient;
import io.arbitrix.core.integration.okx.rest.OkxPlaceOrderClient;
import io.arbitrix.core.strategy.base.action.AbstractExchangeClient;
import io.arbitrix.core.strategy.base.action.SpotStrategy;
import io.arbitrix.core.strategy.base.condition.ExecuteStrategyConditional;
import io.arbitrix.core.strategy.base.enums.ProfitOrderPlaceStrategyEnum;
import io.arbitrix.core.strategy.profit_market_making.data.ProfitMarketMakingSpotOrderTradeDataManager;
import io.arbitrix.core.strategy.profit_market_making.data.SymbolDataHolder;
import io.arbitrix.core.utils.OrderTradeUtil;
import io.arbitrix.core.utils.SystemClock;
import io.arbitrix.core.common.util.JacksonUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static io.arbitrix.core.strategy.base.enums.ProfitOrderPlaceStrategyEnum.*;

/**
 * 固定价差下单策略 <br>
 * (1) 最优报价，按一定价差下单 <br>
 * (2) 跟随N档的价格直接出价 <br>
 *
 * @author jonathan.ji
 */
@Slf4j
@Component
@ExecuteStrategyConditional(executeStrategyName = "profit_market_making")
public class ProfitMarketMakingSpotStrategy extends AbstractExchangeClient implements SpotStrategy {
    private static final BigDecimal BASE_PERCENT = new BigDecimal(1);

    @Value("${market.making.quote-coin-value:}")
    private String symbolQuantity;

    @Value("${market.making.price.spread:}")
    private String symbolInterval;

    private final ProfitMarketMakingSpotOrderTradeDataManager profitOrderTradeDataManager;
    private final OrderBookDepthDistribution orderBookDepthDistribution;
    private final ProfitOrderPlaceStrategy profitOrderPlaceStrategy;
    private final SymbolDataHolder symbolDataHolder;

    private final BigDecimal orderLevelSpread = new BigDecimal(EnvUtil.getProperty("order_level_spread_cmd", "0.00003"));
    private final BigDecimal profitAskPriceBaseOnBestBidPrice = new BigDecimal(EnvUtil.getProperty("profit_ask_price_base_on_best_bid_price_cmd", "0.00002"));

    public ProfitMarketMakingSpotStrategy(ProfitMarketMakingSpotOrderTradeDataManager profitOrderTradeDataManager,
                                          ProfitOrderPlaceStrategy profitOrderPlaceStrategy,
                                          BinanceWebSocketClient binanceWsApiClient,
                                          BitgetRestClient bitgetRestClient, BybitRestClient bybitRestClient,
                                          OkxCancelOrderClient okxCancelOrderClient,
                                          OkxPlaceOrderClient okxPlaceOrderClient,
                                          OrderBookDepthDistribution orderBookDepthDistribution,
                                          SymbolDataHolder symbolDataHolder,
                                          MarketFacade marketFacade) {
        super(marketFacade, binanceWsApiClient, bitgetRestClient, bybitRestClient, okxPlaceOrderClient, okxCancelOrderClient);
        this.profitOrderTradeDataManager = profitOrderTradeDataManager;
        this.orderBookDepthDistribution = orderBookDepthDistribution;
        this.profitOrderPlaceStrategy = profitOrderPlaceStrategy;
        this.symbolDataHolder = symbolDataHolder;
    }

    @Override
    public void execute(SpotOrderExecutionContext context) {
        ReadyExecuteContext readyExecuteContext = readyToExecute(context);
        log.info("FixedSpreadBasedOrderPlace.execute.result is {}", JacksonUtil.toJsonStr(readyExecuteContext));
        if (readyExecuteContext.getCanExecute()) {
            // 创建订单
            List<ExchangeOrder> sportOrderList = this.createSportOrderList(context);
            if (CollectionUtils.isEmpty(sportOrderList)) {
                log.warn("ProfitMarketMakingSpotStrategy.execute.sportOrderList is empty");
                return;
            }

            // 订单保存到缓存次
            profitOrderTradeDataManager.putOrderIntoOrderPool(context, sportOrderList);

            // 下单
            sportOrderList.forEach((sportOrder) -> newOrderByRestAndWss(context.getExchangeName(), sportOrder, Category.SPOT.getCode()));

            long gapTime = SystemClock.now() - context.getBookTickerEvent().getArrivalTime();
            StrategyMonitor.recordMarketMakerExecuteOrderThreadGap(gapTime, context.getExchangeName(), context.getSymbol(), context.getOrderSide().toString());
            log.info("PureMarketMakingSpotWorker.onBookTicker.gapTime:{} CurrentTime is {} arrivalTime is {}", gapTime, SystemClock.now(), context.getBookTickerEvent().getArrivalTime());

            // 取消订单
            List<String> cancelOrderIdList = readyExecuteContext.getCancelOrderIdList();
            if (CollectionUtils.isNotEmpty(cancelOrderIdList)) {
                cancelOrderIdList.forEach((cancelOrderClientId) -> {
                    cancelOrder(context.getExchangeName(), context.getSymbol(), cancelOrderClientId, Category.SPOT.getCode());
                });
            }
        }
    }

    private List<ExchangeOrder> createSportOrderList(SpotOrderExecutionContext context) {
        List<ExchangeOrder> sportOrderList = new ArrayList<>();
        ProfitOrderPlaceStrategyEnum profitOrderPlaceStrategyEnum = profitOrderPlaceStrategy.getOrderPlaceStrategyName();
        if (ORDER_LEVEL_SPREAD_BY_BEST_PRICE.equals(profitOrderPlaceStrategyEnum)) {
            sportOrderList = createSportOrder(context);
        }

        if (FOLLOW_PRICE_BY_TOP_N.equals(profitOrderPlaceStrategyEnum)) {
            sportOrderList = createSportOrderFollowDepthPrice(context);
        }

        if (ASK_PRICE_BASE_ON_BEST_BID_PRICE.equals(profitOrderPlaceStrategyEnum)) {
            if (context.getOrderSide() == OrderSide.SELL) {
                return sportOrderList;
            }
            sportOrderList = createAllSportOrderBaseOnBestAskPrice(context);
        }

        if (PRICE_BASE_ON_USDT_PRICE.equals(profitOrderPlaceStrategyEnum)) {
            sportOrderList = createOrderBaseOnUsdtPrice(context);
        }

        if (OrderSide.BUY == context.getOrderSide()) {
            log.info("createSportOrderList.execute.sportOrderList bidPrice is {} is {}", context.getBookTickerEvent().getBidPrice(), JacksonUtil.toJsonStr(sportOrderList));
        } else {
            log.info("createSportOrderList.execute.sportOrderList askPrice is {} is {}", context.getBookTickerEvent().getAskPrice(), JacksonUtil.toJsonStr(sportOrderList));
        }
        return sportOrderList;
    }

    public ReadyExecuteContext readyToExecute(SpotOrderExecutionContext context) {
        OrderSide orderSide = context.getOrderSide();
        Integer orderLevel = context.getOrderLevel();
        BookTickerEvent bookTickerEvent = context.getBookTickerEvent();

        String orderBookCacheKey = OrderTradeUtil.buildLevelOrderTradeKey(context.getExchangeName(), context.getSymbol(), orderSide, orderLevel);
        OwnOrderBook ownOrderBook = context.getOwnOrderBook().get(orderBookCacheKey);
        if (ownOrderBook == null) {
            // 如果缓存的数据被清理了，需要重新下单
            return new ReadyExecuteContext(true, null, orderLevel);
        }
        List<OrderTradeUpdateEvent> orderTradeUpdateEventList = ownOrderBook.getOrderTradeUpdateEventList();

        // 订单的金额不相等，需要下单，同时需要将上一个订单取消
        BigDecimal bestPriceDecimal = context.isBuy() ? new BigDecimal(bookTickerEvent.getBidPrice()) : new BigDecimal(bookTickerEvent.getAskPrice());

        if (bestPriceDecimal.compareTo(new BigDecimal(ownOrderBook.getAnchorPrice())) != 0) {
            List<String> cancelOrderList = orderTradeUpdateEventList.stream().map(OrderTradeUpdateEvent::getOrigClientOrderId).collect(Collectors.toList());
            return new ReadyExecuteContext(true, cancelOrderList, orderLevel);
        } else {
            // 订单金额相等，但是预设的档位没订单，需要在对应档位下单，这边没考虑到订单的数量，如果存在一单就够的情况
            if (CollectionUtils.isEmpty(orderTradeUpdateEventList)) {
                return new ReadyExecuteContext(true, null, orderLevel);
            }
        }
        return new ReadyExecuteContext(false, null, null);
    }

    /**
     * 买价 = 价格 * (1 - 订单层级 * order_level_spread） <br>
     * 卖价 = 价格 * (1 + 订单层级 * order_level_spread） <br>
     */
    public List<ExchangeOrder> createSportOrder(SpotOrderExecutionContext context) {
        List<ExchangeOrder> sportOrderList = new ArrayList<>();
        Map<String, String> symbolIntervalMap = JacksonUtil.fromMap(symbolQuantity, String.class);

        int orderSize = profitOrderPlaceStrategy.getOrderPlaceQuantity();
        if (context.getOrderSide() == OrderSide.BUY) {
            String buyKey = OrderTradeUtil.buildOrderTradeKey(context.getExchangeName(), context.getSymbol(), OrderSide.BUY);
            String configBuyOrderValue = symbolIntervalMap.getOrDefault(buyKey, "0.1");

            BigDecimal bestBidPrice = new BigDecimal(context.getBookTickerEvent().getBidPrice());
            for (int i = 1; i <= orderSize; i++) {

                // TODO 这个规则我先写死，可以要针对交易所的规则做特定的处理 ETHUSDT.tickSize = 0.01  BTCUSDT.tickSize = 0.1
                // https://api.bybit.com/derivatives/v3/public/instruments-info?category=linear&symbol=BTCUSDT
                BigDecimal price = bestBidPrice.multiply(BASE_PERCENT.subtract(BigDecimal.valueOf(i).multiply(orderLevelSpread)));
                BigDecimal roundedNumber = price.setScale(2, RoundingMode.DOWN);

                String uuid = orderBookDepthDistribution.getUuidByOrderLevel(context.getOrderLevel());
                String buyOrderQuantity = this.calculateQuantity(context.getExchangeName(), context.getSymbol(), Category.SPOT.getCode(), roundedNumber, configBuyOrderValue);
                ExchangeOrder sportOrder = ExchangeOrder.limitMarketBuy(context.getSymbol(), buyOrderQuantity, roundedNumber.toString(), uuid);
                sportOrderList.add(sportOrder);
            }
        }

        if (context.getOrderSide() == OrderSide.SELL) {
            String sellKey = OrderTradeUtil.buildOrderTradeKey(context.getExchangeName(), context.getSymbol(), OrderSide.SELL);
            String configSellOrderValue = symbolIntervalMap.getOrDefault(sellKey, "0.1");


            BigDecimal bestAskPrice = new BigDecimal(context.getBookTickerEvent().getAskPrice());
            for (int i = 1; i <= orderSize; i++) {
                // TODO 这个规则我先写死，可以要针对交易所的规则做特定的处理 ETHUSDT.tickSize = 0.01  BTCUSDT.tickSize = 0.1
                BigDecimal price = bestAskPrice.multiply(BASE_PERCENT.add(BigDecimal.valueOf(i).multiply(orderLevelSpread)));
                BigDecimal roundedNumber = price.setScale(2, RoundingMode.DOWN);

                String uuid = orderBookDepthDistribution.getUuidByOrderLevel(context.getOrderLevel());
                String sellOrderQuantity = this.calculateQuantity(context.getExchangeName(), context.getSymbol(), Category.SPOT.getCode(), roundedNumber, configSellOrderValue);
                ExchangeOrder sportOrder = ExchangeOrder.limitMarketSell(context.getSymbol(), sellOrderQuantity, roundedNumber.toString(), uuid);
                sportOrderList.add(sportOrder);
            }
        }
        return sportOrderList;
    }

    /**
     * 策略二的模式，根据深度价格下单
     */
    public List<ExchangeOrder> createSportOrderFollowDepthPrice(SpotOrderExecutionContext context) {
        List<ExchangeOrder> sportOrderList = new ArrayList<>();
        Map<String, String> symbolIntervalMap = JacksonUtil.fromMap(symbolQuantity, String.class);

        String uuid = orderBookDepthDistribution.getUuidByOrderLevel(context.getOrderLevel());
        if (context.getOrderSide() == OrderSide.BUY) {
            String buyKey = OrderTradeUtil.buildOrderTradeKey(context.getExchangeName(), context.getSymbol(), OrderSide.BUY);
            String buyOrderQuantity = this.calculateQuantity(context.getExchangeName(), context.getSymbol(),
                    Category.SPOT.getCode(), new BigDecimal(context.getBookTickerEvent().getBidPrice()),
                    symbolIntervalMap.getOrDefault(buyKey, "0.1"));
            BookTickerEvent bookTickerEvent = context.getBookTickerEvent();
            ExchangeOrder sportOrder = ExchangeOrder.limitMarketBuy(context.getSymbol(), buyOrderQuantity, bookTickerEvent.getBidPrice(), uuid);
            sportOrderList.add(sportOrder);
        }

        if (context.getOrderSide() == OrderSide.SELL) {
            String sellKey = OrderTradeUtil.buildOrderTradeKey(context.getExchangeName(), context.getSymbol(), OrderSide.SELL);
            String sellOrderQuantity = this.calculateQuantity(context.getExchangeName(), context.getSymbol(),
                    Category.SPOT.getCode(), new BigDecimal(context.getBookTickerEvent().getAskPrice()),
                    symbolIntervalMap.getOrDefault(sellKey, "0.1"));
            BookTickerEvent bookTickerEvent = context.getBookTickerEvent();
            ExchangeOrder sportOrder = ExchangeOrder.limitMarketSell(context.getSymbol(), sellOrderQuantity, bookTickerEvent.getAskPrice(), uuid);
            sportOrderList.add(sportOrder);
        }
        return sportOrderList;
    }

    public List<ExchangeOrder> createAllSportOrderBaseOnBestAskPrice(SpotOrderExecutionContext context) {
        List<ExchangeOrder> sportOrderList = new ArrayList<>();
        Map<String, String> symbolIntervalMap = JacksonUtil.fromMap(symbolQuantity, String.class);

        if (context.getOrderSide() == OrderSide.BUY) {
            String buyKey = OrderTradeUtil.buildOrderTradeKey(context.getExchangeName(), context.getSymbol(), OrderSide.BUY);
            String sellKey = OrderTradeUtil.buildOrderTradeKey(context.getExchangeName(), context.getSymbol(), OrderSide.SELL);


            BookTickerEvent bookTickerEvent = context.getBookTickerEvent();

            BigDecimal bestBidPrice = this.calculateBuyPrice(context);
            String buyOrderQuantity = this.calculateQuantity(context.getExchangeName(), context.getSymbol(),
                    Category.SPOT.getCode(), bestBidPrice,
                    symbolIntervalMap.getOrDefault(buyKey, "0.1"));
            ExchangeOrder sportBuyOrder = ExchangeOrder.limitMarketBuy(context.getSymbol(), buyOrderQuantity, bestBidPrice.toString(), UUID.randomUUID().toString());
            sportOrderList.add(sportBuyOrder);

            BigDecimal bestSellPrice = this.calculateSellPrice(bestBidPrice, new BigDecimal(bookTickerEvent.getAskPrice()));
            String sellOrderQuantity = this.calculateQuantity(context.getExchangeName(), context.getSymbol(),
                    Category.SPOT.getCode(), bestSellPrice,
                    symbolIntervalMap.getOrDefault(sellKey, "0.1"));

            ExchangeOrder sportSellOrder = ExchangeOrder.limitMarketSell(context.getSymbol(), sellOrderQuantity, bestSellPrice.toString(), UUID.randomUUID().toString());
            sportOrderList.add(sportSellOrder);
        }
        return sportOrderList;
    }

    public BigDecimal calculateBuyPrice(SpotOrderExecutionContext context) {
        Map<String, BigDecimal> symbolIntervalMap = this.getSymbolIntervalConfig();
        BigDecimal bidDecimal = context.getBidPrice();
        BigDecimal askDecimal = context.getAskPrice();

        if (context.isBuy()) {
            String bugKey = OrderTradeUtil.buildOrderTradeKey(context.getExchangeName(), context.getSymbol(), context.getOrderSide());
            BigDecimal buyDownInterval = symbolIntervalMap.getOrDefault(bugKey, BigDecimal.ZERO);
            // 如果买单的价格加上买单的间隔，大于等于卖单的价格，说明当前的买价已经是极限价，直接返回该买价
            BigDecimal fixedPrice = bidDecimal.add(buyDownInterval);
            if (fixedPrice.compareTo(askDecimal) >= 0) {
                return bidDecimal;
            }
            return fixedPrice;
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal calculateSellPrice(BigDecimal bestBidPrice, BigDecimal bestAskPrice) {
        BigDecimal price = bestBidPrice.multiply(BASE_PERCENT.add(profitAskPriceBaseOnBestBidPrice));
        BigDecimal fixedPrice = price.setScale(2, RoundingMode.DOWN);
        // 如果基于买单价往上浮动，依然没有达到卖一价，那便以卖一价出
        if (fixedPrice.compareTo(bestAskPrice) <= 0) {
            log.warn("calculateSellPrice.is.lower.than.bestAskPrice, fixedPrice:{}, bestAskPrice:{}", fixedPrice, bestAskPrice);
            return bestAskPrice;
        }
        return fixedPrice;
    }

    public Map<String, BigDecimal> getSymbolIntervalConfig() {
        return JacksonUtil.fromMap(symbolInterval, BigDecimal.class);
    }

    public List<ExchangeOrder> createOrderBaseOnUsdtPrice(SpotOrderExecutionContext context) {
        String symbol = context.getSymbol();
        if ("USDCUSDT".equalsIgnoreCase(symbol) || "BTCUSDT".equalsIgnoreCase(symbol)) {
            return Collections.emptyList();
        }

        List<ExchangeOrder> sportOrderList = new ArrayList<>();
        BookTickerEvent usdcUsdtData = symbolDataHolder.getBookTickerFromCache(context.getExchangeName(), "USDCUSDT");
        BookTickerEvent bctUsdtData = symbolDataHolder.getBookTickerFromCache(context.getExchangeName(), "BTCUSDT");
        Map<String, String> symbolIntervalMap = JacksonUtil.fromMap(symbolQuantity, String.class);
        if (context.getOrderSide() == OrderSide.BUY) {
            String buyKey = OrderTradeUtil.buildOrderTradeKey(context.getExchangeName(), context.getSymbol(), OrderSide.BUY);

            // 计算价格
            String price = calculateBuyPriceOnUsdtPrice(bctUsdtData, usdcUsdtData);
            String buyOrderQuantity = this.calculateQuantity(context.getExchangeName(), context.getSymbol(),
                    Category.SPOT.getCode(), new BigDecimal(price),
                    symbolIntervalMap.getOrDefault(buyKey, "0.1"));
            ExchangeOrder sportBuyOrder = ExchangeOrder.limitMarketBuy(context.getSymbol(), buyOrderQuantity, price, UUID.randomUUID().toString());
            sportOrderList.add(sportBuyOrder);
        }

        if (context.getOrderSide() == OrderSide.SELL) {
            String sellKey = OrderTradeUtil.buildOrderTradeKey(context.getExchangeName(), context.getSymbol(), OrderSide.SELL);

            // 计算价格
            String price = calculateSellPriceOnUsdtPrice(bctUsdtData, usdcUsdtData);
            String sellOrderQuantity = this.calculateQuantity(context.getExchangeName(), context.getSymbol(),
                    Category.SPOT.getCode(), new BigDecimal(price),
                    symbolIntervalMap.getOrDefault(sellKey, "0.1"));
            ExchangeOrder sportBuyOrder = ExchangeOrder.limitMarketSell(context.getSymbol(), sellOrderQuantity, price, UUID.randomUUID().toString());
            sportOrderList.add(sportBuyOrder);
        }
        return sportOrderList;
    }

    public String calculateBuyPriceOnUsdtPrice(BookTickerEvent bctUsdtData, BookTickerEvent usdcUsdtData) {
        // 产品为USDC，但是要按照USDT来计价
        if (bctUsdtData == null || bctUsdtData.getBidPrice() == null) {
            throw new RuntimeException("calculateBuyPriceOnUsdtPrice.bctUsdtData.is.null");
        }
        BigDecimal buyPrice = new BigDecimal(bctUsdtData.getBidPrice());
        BigDecimal ratePrice = (usdcUsdtData != null && usdcUsdtData.getAskPrice() != null) ? new BigDecimal(usdcUsdtData.getAskPrice()) : BigDecimal.ONE;
        BigDecimal targetPrice = buyPrice.divide(ratePrice, 8, RoundingMode.DOWN);

        // 按照报表的价格磨损数据，设置价格浮动
        BigDecimal spreadPrice = targetPrice.multiply(BASE_PERCENT.subtract(orderLevelSpread));

        // TODO 需要根据交易所的精度，来进行精度的处理
        return spreadPrice.setScale(2, RoundingMode.DOWN).toString();
    }

    public String calculateSellPriceOnUsdtPrice(BookTickerEvent bctUsdtData, BookTickerEvent usdcUsdtData) {
        // 产品为USDC，但是要按照USDT来计价
        if (bctUsdtData == null || bctUsdtData.getAskPrice() == null) {
            throw new RuntimeException("calculateSellPriceOnUsdtPrice.bctUsdtData.is.null");
        }
        BigDecimal sellPrice = new BigDecimal(bctUsdtData.getAskPrice());
        BigDecimal ratePrice = (usdcUsdtData != null && usdcUsdtData.getBidPrice() != null) ? new BigDecimal(usdcUsdtData.getBidPrice()) : BigDecimal.ONE;
        BigDecimal targetPrice = sellPrice.divide(ratePrice, 8, RoundingMode.DOWN);

        // 按照报表的价格磨损数据，设置价格浮动
        BigDecimal spreadPrice = targetPrice.multiply(BASE_PERCENT.add(orderLevelSpread));

        // TODO 需要根据交易所的精度，来进行精度的处理
        return spreadPrice.setScale(2, RoundingMode.DOWN).toString();
    }
}
