package io.arbitrix.core.strategy.future_pure_market_making.strategy;

import io.arbitrix.core.common.util.EnvUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.arbitrix.core.common.domain.ExchangeOrder;
import io.arbitrix.core.common.domain.FutureOrderExecutionContext;
import io.arbitrix.core.common.enums.OrderSide;
import io.arbitrix.core.common.event.BookTickerEvent;
import io.arbitrix.core.common.event.OrderTradeUpdateEvent;
import io.arbitrix.core.common.monitor.StrategyMonitor;
import io.arbitrix.core.facade.MarketFacade;
import io.arbitrix.core.integration.binance.wss.BinanceWebSocketClient;
import io.arbitrix.core.integration.bitget.rest.BitgetRestClient;
import io.arbitrix.core.integration.bybit.rest.BybitRestClient;
import io.arbitrix.core.integration.bybit.rest.enums.Category;
import io.arbitrix.core.integration.okx.rest.OkxCancelOrderClient;
import io.arbitrix.core.integration.okx.rest.OkxPlaceOrderClient;
import io.arbitrix.core.strategy.base.action.AbstractExchangeClient;
import io.arbitrix.core.strategy.base.action.FutureStrategy;
import io.arbitrix.core.strategy.base.condition.ExecuteStrategyConditional;
import io.arbitrix.core.strategy.base.enums.FutureExecuteStrategyEnum;
import io.arbitrix.core.strategy.future_pure_market_making.data.FuturePureMarketMakingOrderTradeDataManager;
import io.arbitrix.core.utils.OrderTradeUtil;
import io.arbitrix.core.utils.SystemClock;
import io.arbitrix.core.common.util.JacksonUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.arbitrix.core.strategy.base.enums.FutureExecuteStrategyEnum.PRICE_CHANGE_ON_BEST_PRICE;

/**
 * 抢占卖一位和买一位
 *
 * @author jonathan.ji
 */
@Component
@Log4j2
@ExecuteStrategyConditional(executeStrategyName = "future_pure_market_making")
public class FuturePureMarketMakingStrategy extends AbstractExchangeClient implements FutureStrategy {

    @Value("${market.making.future.quantity:}")
    private String symbolQuantity;

    @Value("${market.making.future.price.spread:}")
    private String priceSpread;

    private final String futurePriceStrategyName = EnvUtil.getProperty("market.making.future.price.strategy");

    private final FuturePureMarketMakingOrderTradeDataManager orderTradeDataManager;

    public FuturePureMarketMakingStrategy(BinanceWebSocketClient binanceWsApiClient,
                                          BitgetRestClient bitgetRestClient,
                                          BybitRestClient bybitRestClient,
                                          OkxPlaceOrderClient okxPlaceOrderClient,
                                          OkxCancelOrderClient okxCancelOrderClient,
                                          FuturePureMarketMakingOrderTradeDataManager orderTradeDataManager,
                                          MarketFacade marketFacade) {
        super(marketFacade, binanceWsApiClient, bitgetRestClient, bybitRestClient, okxPlaceOrderClient, okxCancelOrderClient);
        this.orderTradeDataManager = orderTradeDataManager;
    }

    @Override
    public void execute(FutureOrderExecutionContext context) {
        String futureExecuteStrategyName = context.getFutureExecuteStrategyName();
        OrderSide orderSide = context.getOrderSide();
        // open long
        if (FutureExecuteStrategyEnum.FUTURE_PURE_LONG.getStrategyTag().equals(futureExecuteStrategyName) && OrderSide.BUY.equals(orderSide)) {
            log.info("FuturePureMarketMakingStrategy.open.long context is {}", JacksonUtil.toJsonStr(context));
            this.executeOpenFutureOrder(context);
        }

        // close long
        if (FutureExecuteStrategyEnum.FUTURE_PURE_LONG.getStrategyTag().equals(futureExecuteStrategyName) && OrderSide.SELL.equals(orderSide)) {
            log.info("FuturePureMarketMakingStrategy.close.long context is {}", JacksonUtil.toJsonStr(context));
            this.executeCloseFutureOrder(context);
        }

        // open short
        if (FutureExecuteStrategyEnum.FUTURE_PURE_SHORT.getStrategyTag().equals(futureExecuteStrategyName) && OrderSide.BUY.equals(orderSide)) {
            log.info("FuturePureMarketMakingStrategy.open.short context is {}", JacksonUtil.toJsonStr(context));
            this.executeOpenFutureOrder(context);
        }

        // close short
        if (FutureExecuteStrategyEnum.FUTURE_PURE_SHORT.getStrategyTag().equals(futureExecuteStrategyName) && OrderSide.SELL.equals(orderSide)) {
            log.info("FuturePureMarketMakingStrategy.close.short context is {}", JacksonUtil.toJsonStr(context));
            this.executeCloseFutureOrder(context);
        }
    }

    private void executeOpenFutureOrder(FutureOrderExecutionContext context) {
        Pair<Boolean, String> beginTrade = readyToExecute(context);
        log.info("FuturePureMarketMakingStrategy.onBookTicker.beginTrade:{} orderSide is {}", beginTrade, context.getOrderSide());
        if (beginTrade.getLeft()) {
            // 生成订单，放入订单池（暂时只会生成一个订单）
            List<ExchangeOrder> futureOrderList = this.createFutureOrder(context, "false");
            orderTradeDataManager.putOrderIntoOrderPool(context.getExchangeName(), futureOrderList.get(0));

            // 下单
            newOrderByRestAndWss(context.getExchangeName(), futureOrderList.get(0), Category.LINEAR.getCode());

            long gapTime = SystemClock.now() - context.getBookTickerEvent().getArrivalTime();
            StrategyMonitor.recordMarketMakerExecuteOrderThreadGap(gapTime, context.getExchangeName(), context.getSymbol(), context.getOrderSide().toString());
            log.info("FuturePureMarketMakingStrategy.onBookTicker.gapTime:{} CurrentTime is {} arrivalTime is {}", gapTime, SystemClock.now(), context.getBookTickerEvent().getArrivalTime());

            // 取消上一轮订单
            if (StringUtils.isEmpty(beginTrade.getRight())) {
                return;
            }
            cancelOrder(context.getExchangeName(), context.getSymbol(), beginTrade.getRight(), Category.LINEAR.getCode());
        }
    }

    /**
     * 清仓的逻辑：使用正常的下单接口，需要把side设置为持仓的反方向，并且reduceOnly=true <br>
     */
    private void executeCloseFutureOrder(FutureOrderExecutionContext context) {
        // 生成订单，放入订单池（暂时只会生成一个订单）
        List<ExchangeOrder> futureOrderList = this.createFutureOrder(context, "true");
        newOrderByRestAndWss(context.getExchangeName(), futureOrderList.get(0), Category.LINEAR.getCode());
    }

    public List<ExchangeOrder> createFutureOrder(FutureOrderExecutionContext context, String reduceOnly) {
        List<ExchangeOrder> futureOrderList = new ArrayList<>();
        Map<String, String> symbolIntervalMap = JacksonUtil.fromMap(symbolQuantity, String.class);

        if (context.isBuy()) {
            String buyKey = OrderTradeUtil.buildOrderTradeKey(context.getExchangeName(), context.getSymbol(), OrderSide.BUY);
            String buyOrderQuantity = symbolIntervalMap.getOrDefault(buyKey, "0.1");

            BigDecimal bestBidPrice;
            if (PRICE_CHANGE_ON_BEST_PRICE.getStrategyTag().equalsIgnoreCase(futurePriceStrategyName)) {
                bestBidPrice = this.calculatePrice(context);
                log.info("FuturePureMarketMakingStrategy.createFutureOrder.priceStrategy bestBidPrice is {} oriBestPrice is {} ", bestBidPrice, context.getBidPrice());
            } else {
                bestBidPrice = new BigDecimal(context.getBookTickerEvent().getBidPrice());
            }
            ExchangeOrder futureOrder = ExchangeOrder.limitMarketBuy(context.getSymbol(), buyOrderQuantity, bestBidPrice.toString(), UUID.randomUUID().toString());
            futureOrder.setReduceOnly(reduceOnly);
            futureOrderList.add(futureOrder);
        }

        if (context.isSell()) {
            String sellKey = OrderTradeUtil.buildOrderTradeKey(context.getExchangeName(), context.getSymbol(), OrderSide.SELL);
            String sellOrderQuantity = symbolIntervalMap.getOrDefault(sellKey, "0.1");

            BigDecimal bestAskPrice;
            if (PRICE_CHANGE_ON_BEST_PRICE.getStrategyTag().equalsIgnoreCase(futurePriceStrategyName)) {
                bestAskPrice = this.calculatePrice(context);
                log.info("FuturePureMarketMakingStrategy.createFutureOrder.priceStrategy bestAskPrice is {} oriBestPrice is {} ", bestAskPrice, context.getAskPrice());
            } else {
                bestAskPrice = new BigDecimal(context.getBookTickerEvent().getAskPrice());
            }

            ExchangeOrder futureOrder = ExchangeOrder.limitMarketSell(context.getSymbol(), sellOrderQuantity, bestAskPrice.toString(), UUID.randomUUID().toString());
            futureOrder.setReduceOnly(reduceOnly);
            futureOrderList.add(futureOrder);
        }
        return futureOrderList;
    }

    public BigDecimal calculatePrice(FutureOrderExecutionContext context) {
        Map<String, BigDecimal> symbolIntervalMap = this.getSymbolIntervalConfig();
        BigDecimal bidDecimal = context.getBidPrice();
        BigDecimal askDecimal = context.getAskPrice();

        if (context.isBuy()) {
            String bugKey = OrderTradeUtil.buildOrderTradeKey(context.getExchangeName(), context.getSymbol(), context.getOrderSide());
            BigDecimal buyDownInterval = symbolIntervalMap.getOrDefault(bugKey, BigDecimal.ZERO);
            return bidDecimal.subtract(buyDownInterval);
        }

        if (context.isSell()) {
            String sellKey = OrderTradeUtil.buildOrderTradeKey(context.getExchangeName(), context.getSymbol(), context.getOrderSide());
            BigDecimal sellDownInterval = symbolIntervalMap.getOrDefault(sellKey, BigDecimal.ZERO);
            return askDecimal.add(sellDownInterval);
        }
        return BigDecimal.ZERO;
    }

    public Map<String, BigDecimal> getSymbolIntervalConfig() {
        return JacksonUtil.fromMap(priceSpread, BigDecimal.class);
    }

    private Pair<Boolean, String> readyToExecute(FutureOrderExecutionContext context) {
        OrderSide orderSide = context.getOrderSide();
        BookTickerEvent bookTickerEvent = context.getBookTickerEvent();

        String tradeKey = OrderTradeUtil.buildOrderTradeKey(context.getExchangeName(), context.getSymbol(), orderSide);
        OrderTradeUpdateEvent orderTradeEvent = context.getOrderTradeUpdateEventMap().get(tradeKey);
        if (orderTradeEvent == null) {
            orderTradeEvent = OrderTradeUpdateEvent.createInitOrderTradeUpdateEvent(orderSide);
        }

        if (orderSide == OrderSide.BUY) {
            boolean buyFlag = checkBeginTrade(bookTickerEvent.getBidPrice(), orderTradeEvent);
            return Pair.of(buyFlag, (orderTradeEvent.getOrigClientOrderId() == null ? null : orderTradeEvent.getOrigClientOrderId()));
        }

        if (orderSide == OrderSide.SELL) {
            boolean sellFlag = checkBeginTrade(bookTickerEvent.getAskPrice(), orderTradeEvent);
            return Pair.of(sellFlag, (orderTradeEvent.getOrigClientOrderId() == null ? null : orderTradeEvent.getOrigClientOrderId()));
        }
        return Pair.of(false, null);
    }

    private boolean checkBeginTrade(String bestPrice, OrderTradeUpdateEvent orderTradeBuyEvent) {
        BigDecimal bestPriceDecimal = new BigDecimal(bestPrice);
        BigDecimal targetPriceDecimal = new BigDecimal(orderTradeBuyEvent.getPrice());
        return bestPriceDecimal.compareTo(targetPriceDecimal) != 0;
    }
}
