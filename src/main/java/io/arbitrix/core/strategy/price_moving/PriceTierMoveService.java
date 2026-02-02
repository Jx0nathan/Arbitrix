package io.arbitrix.core.strategy.price_moving;

import io.arbitrix.core.common.util.EnvUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import io.arbitrix.core.common.domain.ExchangeOrder;
import io.arbitrix.core.common.domain.MovingPriceIndex;
import io.arbitrix.core.common.domain.MovingPriceOrderHolder;
import io.arbitrix.core.common.enums.ExchangeNameEnum;
import io.arbitrix.core.common.enums.ExecutionType;
import io.arbitrix.core.common.enums.OrderSide;
import io.arbitrix.core.common.event.BookTickerEvent;
import io.arbitrix.core.common.event.OrderTradeUpdateEvent;
import io.arbitrix.core.facade.MarketFacade;
import io.arbitrix.core.integration.bybit.rest.BybitMarketRestClient;
import io.arbitrix.core.integration.bybit.rest.BybitRestClient;
import io.arbitrix.core.integration.bybit.rest.dto.res.SportTickerInfoRes;
import io.arbitrix.core.integration.bybit.rest.enums.Category;
import io.arbitrix.core.strategy.base.action.OrderTradeUpdateListener;
import io.arbitrix.core.strategy.base.condition.ExecuteStrategyConditional;
import io.arbitrix.core.utils.ExchangeMarketOpenUtilV2;
import io.arbitrix.core.utils.executor.MovingPriceCancelOrderExecutor;
import io.arbitrix.core.utils.executor.MovingPricePlaceOrderExecutor;
import io.arbitrix.core.common.util.JacksonUtil;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Log4j2
@Component
@ExecuteStrategyConditional(executeStrategyName = "moving_price")
public class PriceTierMoveService implements OrderTradeUpdateListener {
    private final BybitMarketRestClient bybitMarketRestClient;
    private final BybitRestClient bybitRestClient;
    private final MarketFacade marketFacade;
    private final ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil;
    private final int movingPriceLevel;
    private final BigDecimal minChangeBase;
    private final String quoteCoinValue;
    private final String symbol;
    private final BigDecimal threshold;
    private static final Map<String, ExchangeOrder> orderMap = new ConcurrentHashMap<>();
    private final ThreadPoolExecutor PLACE_ORDER_EXECUTOR = MovingPricePlaceOrderExecutor.getInstance();
    private final ThreadPoolExecutor CANCEL_ORDER_EXECUTOR = MovingPriceCancelOrderExecutor.getInstance();
    public static final String MOVING_PRICE_LEVEL = "moving_price_level";
    public static final String MOVING_PRICE_CHANGE_BASE = "moving_price_change_base";
    public static final String MOVING_PRICE_QUOTE_COIN_VALUE = "moving_price_quote_coin_value";
    public static final String MOVING_PRICE_CORRECTION_THRESHOLD = "moving_price_correction_threshold";
    public final static String ORDER_LEVEL_KEY = "order_level_key:%s:%s";

    public PriceTierMoveService(BybitMarketRestClient bybitMarketRestClient, ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil, BybitRestClient bybitRestClient, MarketFacade marketFacade) {
        this.bybitRestClient = bybitRestClient;
        this.bybitMarketRestClient = bybitMarketRestClient;
        this.exchangeMarketOpenUtil = exchangeMarketOpenUtil;
        this.marketFacade = marketFacade;
        this.movingPriceLevel = Integer.parseInt(EnvUtil.getProperty(MOVING_PRICE_LEVEL));
        this.minChangeBase = new BigDecimal(EnvUtil.getProperty(MOVING_PRICE_CHANGE_BASE));
        this.quoteCoinValue = String.valueOf(EnvUtil.getProperty(MOVING_PRICE_QUOTE_COIN_VALUE));
        this.threshold = new BigDecimal(EnvUtil.getProperty(MOVING_PRICE_CORRECTION_THRESHOLD));

        List<String> symbolList = exchangeMarketOpenUtil.getSymbolPairs(ExchangeNameEnum.BYBIT);
        this.symbol = symbolList.get(0);

    }

    @PostConstruct
    public void init() {
        // 服务初始化的时候创建订单
        this.createOrder();
    }

    public Map<String, ExchangeOrder> getPriceTierOrderMap() {
        return orderMap;
    }

    public void createOrder() {
        BigDecimal marketPrice = this.getMarkerPrice();
        if (marketPrice != null) {
            // 创建订单
            List<ExchangeOrder> exchangeOrderList = this.createOrderByMarkerPrice(marketPrice);
            for (ExchangeOrder item : exchangeOrderList) {
                PLACE_ORDER_EXECUTOR.execute(() -> bybitRestClient.placeOrder(item, Category.SPOT.getCode()));
            }
            List<ExchangeOrder> baseOrderList = this.getSortedBaseOrderList();
            log.info("createOrder.base.on.market.price.is {} oriOrder is {} ", marketPrice, JacksonUtil.toJsonStr(baseOrderList));
        } else {
            throw new RuntimeException("PriceTierMoveService.createOrder.marketPrice is null");
        }
    }

    private List<ExchangeOrder> createOrderByMarkerPrice(BigDecimal marketPrice) {
        List<ExchangeOrder> exchangeOrderList = new ArrayList<>();
        BigDecimal minimumChangePrice = this.calculateDiffPrice(marketPrice);

        // 生成买单
        for (int i = 1; i < movingPriceLevel + 1; i++) {
            String uuid = UUID.randomUUID().toString();
            BigDecimal price = marketPrice.subtract(minimumChangePrice.multiply(BigDecimal.valueOf(i)));
            String buyQuantity = this.calculateQuantity(symbol, price, quoteCoinValue);
            ExchangeOrder buyOrder = ExchangeOrder.limitMarketBuy(symbol, buyQuantity, price.toString(), uuid);
            buyOrder.setPriceLevel(i);
            // 放入到订单缓存中
            String key = String.format(ORDER_LEVEL_KEY, OrderSide.BUY.getValue(), i);
            orderMap.put(key, buyOrder);
            exchangeOrderList.add(buyOrder);
        }

        // 生成卖单
        for (int i = 1; i < movingPriceLevel + 1; i++) {
            String uuid = UUID.randomUUID().toString();
            BigDecimal price = marketPrice.add(minimumChangePrice.multiply(BigDecimal.valueOf(i)));
            String sellQuantity = this.calculateQuantity(symbol, price, quoteCoinValue);
            ExchangeOrder sellOrder = ExchangeOrder.limitMarketSell(symbol, sellQuantity, price.toString(), uuid);
            sellOrder.setPriceLevel(i);
            // 放入到订单缓存中
            String key = String.format(ORDER_LEVEL_KEY, OrderSide.SELL.getValue(), i);
            orderMap.put(key, sellOrder);
            exchangeOrderList.add(sellOrder);
        }
        return exchangeOrderList;
    }

    @Override
    public void orderTradeUpdateEvent(String exchangeName, OrderTradeUpdateEvent event) {
        String clientId = event.getOrigClientOrderId();
        if (event.getExecutionType() == ExecutionType.TRADE) {
            List<ExchangeOrder> baseOrderList = this.getSortedBaseOrderList();
            Pair<ExchangeOrder, MovingPriceIndex> result = this.getOrderByClientId(clientId);
            if (result.getLeft() != null) {
                log.debug("PriceTierMoveService.orderTradeUpdateEvent.exchangeOrder is {}", JacksonUtil.toJsonStr(result.getLeft()));
                // 获得当前缓存中的订单，按照价格由低到高排序。同时计算得到当前价格所在的层级
                ExchangeOrder exchangeOrder = result.getLeft();
                MovingPriceIndex movingPriceIndex = result.getRight();

                // 用户订单成交触发下一次挂单
                BigDecimal finalPrice = new BigDecimal(event.getPrice());
                MovingPriceOrderHolder holder = this.redefineOrderLevel(finalPrice, clientId, movingPriceIndex, exchangeOrder.getSide(), baseOrderList);
                log.info("client id is {} finalPrice is {} orderTradeUpdateEvent.finally.order is {}", clientId, finalPrice, JacksonUtil.toJsonStr(this.getSortedBaseOrderList()));
                this.processOrder(holder.getCancelOrderList(), holder.getCreateOrderList(), clientId, movingPriceIndex.getLevel(), exchangeOrder.getSide());

                // 监测一下是否偏移市场价太多
                BigDecimal marketPrice = getMarkerPriceInCache();
                BigDecimal diffPrice = finalPrice.subtract(marketPrice).abs();
                if (diffPrice.compareTo(threshold) > 0) {
                    log.error("PriceTierMoveService.orderTradeUpdateEvent.diffPrice is {} marketPrice is {} finalPrice is {}", diffPrice, marketPrice, finalPrice);
                }
            } else {
                log.warn("PriceTierMoveService.orderTradeUpdateEvent.exchangeOrder is null event is {}", JacksonUtil.toJsonStr(event));
            }
        }
    }

    public Pair<ExchangeOrder, MovingPriceIndex> getOrderByClientId(String clientId) {
        MovingPriceIndex movingPriceIndex = new MovingPriceIndex();
        ExchangeOrder exchangeOrder = null;

        // 获取买单最低价和卖单最高价
        String minBuyKey = String.format(ORDER_LEVEL_KEY, OrderSide.BUY.getValue(), movingPriceLevel);
        String maxSellKey = String.format(ORDER_LEVEL_KEY, OrderSide.SELL.getValue(), movingPriceLevel);

        BigDecimal minBuyPrice = new BigDecimal(orderMap.get(minBuyKey).getPrice());
        BigDecimal maxSellPrice = new BigDecimal(orderMap.get(maxSellKey).getPrice());

        for (Map.Entry<String, ExchangeOrder> entry : orderMap.entrySet()) {
            ExchangeOrder value = entry.getValue();
            if (clientId.equals(value.getNewClientOrderId())) {
                exchangeOrder = value;
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

    /**
     * 计算订单价格所处的层级
     */
    public MovingPriceIndex calculatePriceLevel(List<ExchangeOrder> baseOrderList, OrderSide side, String orderPrice) {
        int mid = baseOrderList.size() / 2;
        int start = (side == OrderSide.BUY) ? 0 : mid;
        int end = (side == OrderSide.BUY) ? mid : baseOrderList.size();

        String ask1Price = baseOrderList.get(mid).getPrice();
        String bid1Price = baseOrderList.get(mid - 1).getPrice();

        int level = 0;
        for (int i = start; i < end; i++) {
            if (baseOrderList.get(i).getPrice().equals(orderPrice)) {
                level = i;
                break;
            }
        }
        level = (side == OrderSide.BUY) ? Math.abs(level - movingPriceLevel) : Math.abs(level - movingPriceLevel) + 1;

        // 如果价格处于临街值，重新用市场价定义订单的层级，0表示用市场价重新计算
        if (level != mid) {
            return new MovingPriceIndex(level, ask1Price, bid1Price);
        }
        return new MovingPriceIndex(0, ask1Price, bid1Price);
    }

    public MovingPriceOrderHolder redefineOrderLevel(BigDecimal finalPrice, String clientId, MovingPriceIndex movingPriceIndex, OrderSide orderSide, List<ExchangeOrder> baseOrderList) {
        MovingPriceOrderHolder movingPriceOrderHolder = new MovingPriceOrderHolder();

        int levelIndex = movingPriceIndex.getLevel();
        BigDecimal bid1Decimal = new BigDecimal(movingPriceIndex.getBid1Price());
        BigDecimal ask1Decimal = new BigDecimal(movingPriceIndex.getAsk1Price());

        List<ExchangeOrder> cancelOrderList = new ArrayList<>();
        List<ExchangeOrder> createOrderList = new ArrayList<>();
        if (orderSide == OrderSide.BUY) {
            cancelOrderList = classifyCancelOrderInLowerFlow(levelIndex, baseOrderList);
            createOrderList = classifyCreateOrderInLowerFlow(finalPrice, levelIndex, bid1Decimal, ask1Decimal, clientId);
            classifyMovingOrderInLowerFlow(levelIndex, baseOrderList);
        }

        if (orderSide == OrderSide.SELL) {
            cancelOrderList = classifyCancelOrderInUpperFlow(levelIndex, baseOrderList);
            createOrderList = classifyCreateOrderInUpperFlow(finalPrice, levelIndex, bid1Decimal, ask1Decimal, clientId);
            classifyMovingOrderInUpperFlow(levelIndex, baseOrderList);
        }

        movingPriceOrderHolder.setCancelOrderList(cancelOrderList);
        movingPriceOrderHolder.setCreateOrderList(createOrderList);
        return movingPriceOrderHolder;
    }

    public void processOrder(List<ExchangeOrder> cancelOrderList, List<ExchangeOrder> createOrderList, String alreadyTriggerClientId, int level, OrderSide side) {
        log.info("cancelOrderList.size.is {} createOrderList.size.is {} OrderSide is {} level.is {}", cancelOrderList.size(), createOrderList.size(), side, level);
        // 取消的订单以及成交的订单不要主动删除订单缓存，直接后续数据覆盖，否则会有问题
        for (ExchangeOrder item : cancelOrderList) {
            CANCEL_ORDER_EXECUTOR.execute(() -> bybitRestClient.cancel(symbol, Category.SPOT.getCode(), item.getNewClientOrderId()));
        }

        for (ExchangeOrder item : createOrderList) {
            PLACE_ORDER_EXECUTOR.execute(() -> {
                        bybitRestClient.placeOrder(item, Category.SPOT.getCode());
                    }
            );
        }
    }

    public List<ExchangeOrder> classifyCancelOrderInLowerFlow(int levelIndex, List<ExchangeOrder> baseOrderList) {
        // 如果层级为0表示挂单已被击穿，当前挂单全部取消，重新计算
        if (levelIndex == 0) {
            return baseOrderList.subList(baseOrderList.size() - movingPriceLevel, baseOrderList.size());
        }
        return baseOrderList.subList(baseOrderList.size() - levelIndex, baseOrderList.size());
    }

    public List<ExchangeOrder> classifyCancelOrderInUpperFlow(int levelIndex, List<ExchangeOrder> baseOrderList) {
        // 如果层级为0表示挂单已被击穿，当前挂单全部取消，重新计算
        if (levelIndex == 0) {
            return baseOrderList.subList(0, movingPriceLevel);
        }
        return baseOrderList.subList(0, levelIndex);
    }

    public List<ExchangeOrder> classifyMovingOrderInLowerFlow(int levelIndex, List<ExchangeOrder> baseOrderList) {
        List<ExchangeOrder> exchangeOrderList = new ArrayList<>();
        if (levelIndex == 0) {
            return exchangeOrderList;
        }
        int mid = baseOrderList.size() / 2;

        List<ExchangeOrder> buyOrderList = baseOrderList.subList(0, mid - levelIndex);
        for (ExchangeOrder item : buyOrderList) {
            int newLevel = item.getPriceLevel() - levelIndex;
            String key = String.format(ORDER_LEVEL_KEY, OrderSide.BUY.getValue(), newLevel);
            item.setPriceLevel(newLevel);
            orderMap.put(key, item);
        }
        exchangeOrderList.addAll(buyOrderList);

        List<ExchangeOrder> sellOrderList = baseOrderList.subList(mid, baseOrderList.size() - levelIndex);
        for (ExchangeOrder item : sellOrderList) {
            int newLevel = item.getPriceLevel() + levelIndex;
            String key = String.format(ORDER_LEVEL_KEY, OrderSide.SELL.getValue(), newLevel);
            item.setPriceLevel(newLevel);
            orderMap.put(key, item);
        }
        exchangeOrderList.addAll(sellOrderList);
        return exchangeOrderList;
    }

    public List<ExchangeOrder> classifyMovingOrderInUpperFlow(int levelIndex, List<ExchangeOrder> baseOrderList) {
        List<ExchangeOrder> exchangeOrderList = new ArrayList<>();
        if (levelIndex == 0) {
            return exchangeOrderList;
        }
        int mid = baseOrderList.size() / 2;

        List<ExchangeOrder> buyOrderList = baseOrderList.subList(levelIndex, mid);
        for (ExchangeOrder item : buyOrderList) {
            int newLevel = item.getPriceLevel() + levelIndex;
            String key = String.format(ORDER_LEVEL_KEY, OrderSide.BUY.getValue(), newLevel);
            item.setPriceLevel(newLevel);
            orderMap.put(key, item);
        }
        exchangeOrderList.addAll(buyOrderList);

        List<ExchangeOrder> sellOrderList = baseOrderList.subList(mid + levelIndex, baseOrderList.size());
        for (ExchangeOrder item : sellOrderList) {
            int newLevel = item.getPriceLevel() - levelIndex;
            String key = String.format(ORDER_LEVEL_KEY, OrderSide.SELL.getValue(), newLevel);
            item.setPriceLevel(newLevel);
            orderMap.put(key, item);
        }
        exchangeOrderList.addAll(sellOrderList);
        return exchangeOrderList;
    }

    public List<ExchangeOrder> classifyCreateOrderInLowerFlow(BigDecimal finalPrice, int levelIndex, BigDecimal bid1Price, BigDecimal ask1Price, String orderClientId) {
        List<ExchangeOrder> createOrderList = new ArrayList<>();

        // 如果层级为0表示挂单已被击穿，此时需要重新拿市场价做基础定价
        if (levelIndex == 0) {
            BigDecimal marketPrice = getMarkerPriceInCache();
            log.info("classifyCreateOrderInLowerFlow.breakthrough.order.book marketPrice is {} finalPrice is {} orderClientId is {}", marketPrice, finalPrice, orderClientId);
            return this.createOrderByMarkerPrice(marketPrice);
        }

        BigDecimal minimumChangePrice = this.calculateDiffPrice(finalPrice);
        for (int i = 0; i < levelIndex; i++) {
            String buyUuid = UUID.randomUUID().toString();
            BigDecimal buyPrice = bid1Price.subtract(minimumChangePrice.multiply(BigDecimal.valueOf(movingPriceLevel + i)));
            String buyQuantity = this.calculateQuantity(symbol, buyPrice, quoteCoinValue);
            ExchangeOrder buyOrder = ExchangeOrder.limitMarketBuy(symbol, buyQuantity, buyPrice.toString(), buyUuid);

            // 计算当前的买单的层级，并且放入到订单缓存中
            int buyLevel = movingPriceLevel - levelIndex + i + 1;
            String buyOrderLevel = String.format(ORDER_LEVEL_KEY, OrderSide.BUY.getValue(), buyLevel);
            buyOrder.setPriceLevel(buyLevel);
            orderMap.put(buyOrderLevel, buyOrder);
            createOrderList.add(buyOrder);

            String sellUuid = UUID.randomUUID().toString();
            BigDecimal sellPrice = ask1Price.subtract(minimumChangePrice.multiply(BigDecimal.valueOf(i + 1)));
            String sellQuantity = this.calculateQuantity(symbol, sellPrice, quoteCoinValue);
            ExchangeOrder sellOrder = ExchangeOrder.limitMarketSell(symbol, sellQuantity, sellPrice.toString(), sellUuid);

            // 计算当前的卖单的层级，并且放入到订单缓存中
            int sellLevel = levelIndex - i;
            String sellOrderLevel = String.format(ORDER_LEVEL_KEY, OrderSide.SELL.getValue(), sellLevel);
            sellOrder.setPriceLevel(sellLevel);
            orderMap.put(sellOrderLevel, sellOrder);
            createOrderList.add(sellOrder);
        }
        return createOrderList;
    }

    public List<ExchangeOrder> classifyCreateOrderInUpperFlow(BigDecimal finalPrice, int levelIndex, BigDecimal bid1Price, BigDecimal ask1Price, String orderClientId) {
        List<ExchangeOrder> createOrderList = new ArrayList<>();

        // 如果层级为0表示挂单已被击穿，此时需要重新拿市场价做基础定价
        if (levelIndex == 0) {
            BigDecimal marketPrice = getMarkerPriceInCache();
            log.info("orderClientId is {} classifyCreateOrderInUpperFlow.breakthrough.order.book marketPrice is {} finalPrice is {}", orderClientId, marketPrice, finalPrice);
            return this.createOrderByMarkerPrice(marketPrice);
        }

        BigDecimal minimumChangePrice = this.calculateDiffPrice(finalPrice);
        for (int i = 0; i < levelIndex; i++) {
            String buyUuid = UUID.randomUUID().toString();
            BigDecimal buyPrice = bid1Price.add(minimumChangePrice.multiply(BigDecimal.valueOf(i + 1)));
            String buyQuantity = this.calculateQuantity(symbol, buyPrice, quoteCoinValue);
            ExchangeOrder buyOrder = ExchangeOrder.limitMarketBuy(symbol, buyQuantity, buyPrice.toString(), buyUuid);

            // 计算当前的买单的层级，并且放入到订单缓存中
            int buyLevel = levelIndex - i;
            String buyOrderLevel = String.format(ORDER_LEVEL_KEY, OrderSide.BUY.getValue(), buyLevel);
            buyOrder.setPriceLevel(buyLevel);
            orderMap.put(buyOrderLevel, buyOrder);
            createOrderList.add(buyOrder);

            String sellUuid = UUID.randomUUID().toString();
            BigDecimal sellPrice = ask1Price.add(minimumChangePrice.multiply(BigDecimal.valueOf(movingPriceLevel + i)));
            String sellQuantity = this.calculateQuantity(symbol, sellPrice, quoteCoinValue);
            ExchangeOrder sellOrder = ExchangeOrder.limitMarketSell(symbol, sellQuantity, sellPrice.toString(), sellUuid);

            // 计算当前的卖单的层级，并且放入到订单缓存中
            int sellLevel = movingPriceLevel - levelIndex + i + 1;
            String sellOrderLevel = String.format(ORDER_LEVEL_KEY, OrderSide.SELL.getValue(), sellLevel);
            sellOrder.setPriceLevel(sellLevel);
            orderMap.put(sellOrderLevel, sellOrder);
            createOrderList.add(sellOrder);
        }
        return createOrderList;
    }

    private List<ExchangeOrder> getSortedBaseOrderList() {
        // 获取所有订单，并且按由低到高排序
        return orderMap.values().stream().sorted(Comparator.comparing(order -> {
            try {
                return new BigDecimal(order.getPrice());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Price is not a valid integer", e);
            }
        })).collect(Collectors.toList());
    }

    private BigDecimal getMarkerPrice() {
        SportTickerInfoRes sportTickerInfoRes = bybitMarketRestClient.getTickerInfo(Category.SPOT.getCode(), symbol);
        if (sportTickerInfoRes != null && !CollectionUtils.isEmpty(sportTickerInfoRes.getList())) {
            return new BigDecimal(sportTickerInfoRes.getList().get(0).getLastPrice());
        }
        return null;
    }

    private BigDecimal getMarkerPriceInCache() {
        BookTickerEvent bookTickerEvent = marketFacade.lastTicker(ExchangeNameEnum.BYBIT.name(), symbol);
        if (bookTickerEvent != null) {
            return new BigDecimal(bookTickerEvent.getLastPrice());
        }
        throw new RuntimeException("PriceTierMoveService.getMarkerPriceInCache.currentMarketPrice is null");
    }

    public String calculateQuantity(String symbol, BigDecimal price, String quoteCoinValue) {
        return marketFacade.getSymbolLimitInfo(ExchangeNameEnum.BYBIT.name(), Category.SPOT.getCode(), symbol).quoteCoinValue2BaseCoinValueFloor(price, quoteCoinValue);
    }

    public BigDecimal calculateDiffPrice(BigDecimal marketPrice) {
        // 这边的精度先写死
        return marketPrice.divide(this.minChangeBase, 2, RoundingMode.DOWN);
    }
}
