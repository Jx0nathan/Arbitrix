package io.arbitrix.core.strategy.pure_market_making.order;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.arbitrix.core.common.domain.ExchangeOrder;
import io.arbitrix.core.common.domain.SpotOrderExecutionContext;
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
import io.arbitrix.core.strategy.base.action.SpotStrategy;
import io.arbitrix.core.strategy.base.condition.ExecuteStrategyConditional;
import io.arbitrix.core.strategy.pure_market_making.data.PureMarketMakingSpotOrderTradeDataManager;
import io.arbitrix.core.utils.OrderTradeUtil;
import io.arbitrix.core.utils.SystemClock;
import io.arbitrix.core.common.util.JacksonUtil;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * 抢占卖一位和买一位
 *
 * @author jonathan.ji
 */
@Component
@Log4j2
@ExecuteStrategyConditional(executeStrategyName = "pure_market_making")
public class PureMarketMakingSpotStrategy extends AbstractExchangeClient implements SpotStrategy {
    private final PureMarketMakingSpotOrderTradeDataManager pureMarketMakingSpotOrderTradeDataManager;
    @Value("${market.making.price.spread:}")
    private String symbolInterval;
    @Value("${market.making.quote-coin-value:}")
    private String symbolQuantity;

    public PureMarketMakingSpotStrategy(BinanceWebSocketClient binanceWsApiClient,
                                        BitgetRestClient bitgetRestClient,
                                        BybitRestClient bybitRestClient,
                                        OkxPlaceOrderClient okxPlaceOrderClient,
                                        OkxCancelOrderClient okxCancelOrderClient,
                                        PureMarketMakingSpotOrderTradeDataManager pureMarketMakingSpotOrderTradeDataManager,
                                        MarketFacade marketFacade) {
        super(marketFacade, binanceWsApiClient, bitgetRestClient, bybitRestClient, okxPlaceOrderClient, okxCancelOrderClient);
        this.pureMarketMakingSpotOrderTradeDataManager = pureMarketMakingSpotOrderTradeDataManager;
    }

    @Override
    public void execute(SpotOrderExecutionContext context) {
        Pair<Boolean, String> beginTrade = readyToExecute(context);
        log.info("PureMarketMakingSpotWorker.onBookTicker.beginTrade:{} orderSide is {}", beginTrade, context.getOrderSide());
        if (beginTrade.getLeft()) {
            ExchangeOrder sportNewOrder = getOrders(context);
            // 保存订单到缓存池
            pureMarketMakingSpotOrderTradeDataManager.putNewOrderIntoOrderPool(context.getExchangeName(), sportNewOrder);
            // 通过rest和wss下单
            newOrderByRestAndWss(context.getExchangeName(), sportNewOrder, Category.SPOT.getCode());

            long gapTime = SystemClock.now() - context.getBookTickerEvent().getArrivalTime();
            StrategyMonitor.recordMarketMakerExecuteOrderThreadGap(gapTime, context.getExchangeName(), context.getSymbol(), context.getOrderSide().toString());
            log.info("PureMarketMakingSpotWorker.onBookTicker.gapTime:{} CurrentTime is {} arrivalTime is {}", gapTime, SystemClock.now(), context.getBookTickerEvent().getArrivalTime());

            if (StringUtils.isEmpty(beginTrade.getRight())) {
                //有可能订单池中的数据被定时任务清除了会出现这种情况,就不用取消订单了
                return;
            }
            cancelOrder(context.getExchangeName(), context.getSymbol(), beginTrade.getRight(), Category.SPOT.getCode());
        }
    }

    private Pair<Boolean, String> readyToExecute(SpotOrderExecutionContext context) {
        BookTickerEvent bookTickerEvent = context.getBookTickerEvent();

        String tradeKey = OrderTradeUtil.buildOrderTradeKey(context.getExchangeName(), context.getSymbol(), context.getOrderSide());
        OrderTradeUpdateEvent orderTradeEvent = context.getOrderTradeUpdateEventMap().get(tradeKey);
        if (orderTradeEvent == null) {
            // 为什么在订单池中找不到对应的数据？有可能订单池中的数据被定时任务清除了
            orderTradeEvent = OrderTradeUpdateEvent.createInitOrderTradeUpdateEvent(context.getOrderSide());
        }
        if (context.isBuy()) {
            boolean buyFlag = checkBeginTradeForBuy(bookTickerEvent.getBidPrice(), orderTradeEvent);
            return Pair.of(buyFlag, (orderTradeEvent.getOrigClientOrderId() == null ? null : orderTradeEvent.getOrigClientOrderId()));
        }

        if (context.isSell()) {
            boolean sellFlag = checkBeginTradeForSell(bookTickerEvent.getAskPrice(), orderTradeEvent);
            return Pair.of(sellFlag, (orderTradeEvent.getOrigClientOrderId() == null ? null : orderTradeEvent.getOrigClientOrderId()));
        }
        return Pair.of(false, null);
    }

    /**
     * 只有当前价格小于最优价格，或者当前价格等于最优价格但数量不相等时，才可以进行买入交易
     * <p>
     * 先比较数量，再比较价格，能减少一次BigDecimal的比较
     * <p>
     * (1) 这里出现过一个BUG, bestPrice=0.1, targetPrice=0.100  所以单纯用String equals方法会有问题
     * (2) 如果当前挂买单价为: 26934.07 然后挂单失败 然后价格一路走低 为了保证我们误以为还处于最优报价，需要做次校验。卖单的最优报价如果小于当前买单挂单价，也需要重新挂单
     */
    private boolean checkBeginTradeForBuy(String bestPrice, OrderTradeUpdateEvent orderTradeBuyEvent) {
        BigDecimal bestPriceDecimal = new BigDecimal(bestPrice);
        BigDecimal targetPriceDecimal = new BigDecimal(orderTradeBuyEvent.getPrice());
        return bestPriceDecimal.compareTo(targetPriceDecimal) != 0;
    }

    /**
     * 只有当前价格大于目标价格，或者当前价格等于目标价格但数量不相等时，才可以进行卖出交易
     * <p>
     * 先比较数量，再比较价格，能减少一次BigDecimal的比较
     * <p>
     * (1) 这里出现过一个BUG, bestPrice=0.1, targetPrice=0.100  所以单纯用String equals方法会有问题
     * (2) 如果当前挂卖单价为: 26934.07 然后挂单失败 然后价格一路走高 为了保证我们误以为还处于最优报价，需要做次校验。买单的最优报价如果大于当前卖单挂单价，也需要重新挂单
     */
    public boolean checkBeginTradeForSell(String bestPrice, OrderTradeUpdateEvent orderTradeSellEvent) {
        BigDecimal bestPriceDecimal = new BigDecimal(bestPrice);
        BigDecimal targetPriceDecimal = new BigDecimal(orderTradeSellEvent.getPrice());
        return bestPriceDecimal.compareTo(targetPriceDecimal) != 0;
    }

    private ExchangeOrder getOrders(SpotOrderExecutionContext context) {
        Map<String, String> symbolIntervalMap = JacksonUtil.fromMap(symbolQuantity, String.class);
        if (context.isBuy()) {
            BigDecimal targetPrice = this.calculatePrice(context);
            String buyKey = OrderTradeUtil.buildOrderTradeKey(context.getExchangeName(), context.getSymbol(), context.getOrderSide());
            String buyOrderQuantity = this.calculateQuantity(context.getExchangeName(), context.getSymbol(),
                    Category.SPOT.getCode(), targetPrice, symbolIntervalMap.getOrDefault(buyKey, "0.1"));
            return ExchangeOrder.limitMarketBuy(context.getSymbol(), buyOrderQuantity, targetPrice.toString(), UUID.randomUUID().toString());
        }

        if (context.isSell()) {
            BigDecimal targetPrice = this.calculatePrice(context);
            String sellKey = OrderTradeUtil.buildOrderTradeKey(context.getExchangeName(), context.getSymbol(), context.getOrderSide());
            String sellOrderQuantity = this.calculateQuantity(context.getExchangeName(), context.getSymbol(),
                    Category.SPOT.getCode(), targetPrice, symbolIntervalMap.getOrDefault(sellKey, "0.1"));
            return ExchangeOrder.limitMarketSell(context.getSymbol(), sellOrderQuantity, targetPrice.toString(), UUID.randomUUID().toString());
        }
        return null;
    }

    public BigDecimal calculatePrice(SpotOrderExecutionContext context) {
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

        if (context.isSell()) {
            String sellKey = OrderTradeUtil.buildOrderTradeKey(context.getExchangeName(), context.getSymbol(), context.getOrderSide());
            BigDecimal sellDownInterval = symbolIntervalMap.getOrDefault(sellKey, BigDecimal.ZERO);
            // 如果卖单的价格减去卖单的间隔，小于等于买单的价格，说明当前的卖价已经是极限价，直接返回该卖价
            BigDecimal fixedPrice = askDecimal.subtract(sellDownInterval);
            if (fixedPrice.compareTo(bidDecimal) <= 0) {
                return askDecimal;
            }
            return fixedPrice;
        }
        return BigDecimal.ZERO;
    }

    public Map<String, BigDecimal> getSymbolIntervalConfig() {
        return JacksonUtil.fromMap(symbolInterval, BigDecimal.class);
    }
}
