package io.arbitrix.core.strategy.price_moving;

import io.arbitrix.core.common.util.EnvUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import io.arbitrix.core.common.domain.ExchangeOrder;
import io.arbitrix.core.common.enums.ExchangeNameEnum;
import io.arbitrix.core.common.enums.OrderSide;
import io.arbitrix.core.common.event.BookTickerEvent;
import io.arbitrix.core.facade.MarketFacade;
import io.arbitrix.core.utils.BigDecimalUtil;
import io.arbitrix.core.utils.ExchangeMarketOpenUtilV2;
import io.arbitrix.core.utils.executor.NamedThreadFactory;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static io.arbitrix.core.strategy.price_moving.PriceTierMoveService.*;

@Log4j2
@Component
public class PriceCorrectionSchedule {
    private final ScheduledExecutorService SCHEDULER = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("PriceCorrectionService", true));
    private final PriceTierMoveService priceTierMoveService;
    private final ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil;
    private final int movingPriceLevel;
    private final MarketFacade marketFacade;
    private final BigDecimal priceRange;
    private final BigDecimal threshold;
    private final String symbol;

    public PriceCorrectionSchedule(PriceTierMoveService priceTierMoveService, MarketFacade marketFacade, ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil) {
        this.priceTierMoveService = priceTierMoveService;
        this.marketFacade = marketFacade;
        this.exchangeMarketOpenUtil = exchangeMarketOpenUtil;

        this.movingPriceLevel = Integer.parseInt(EnvUtil.getProperty(MOVING_PRICE_LEVEL));
        this.priceRange = new BigDecimal(EnvUtil.getProperty(MOVING_PRICE_CHANGE_BASE));
        this.threshold = new BigDecimal(EnvUtil.getProperty(MOVING_PRICE_CORRECTION_THRESHOLD));

        List<String> symbolList = exchangeMarketOpenUtil.getSymbolPairs(ExchangeNameEnum.BYBIT);
        this.symbol = symbolList.get(0);
    }

    public void start() {
        SCHEDULER.scheduleAtFixedRate(() -> {
            try {
                Map<String, ExchangeOrder> exchangeOrderMap = priceTierMoveService.getPriceTierOrderMap();
                if (exchangeOrderMap.size() != movingPriceLevel * 2) {
                    log.error("PriceCorrectionService.start exchangeOrderList.size is not equal to movingPriceLevel * 2,exchangeOrderList.size is {}", exchangeOrderMap.size());
                    return;
                }
                // 检查订单薄中的订单是不是按买卖依次递增
                // this.regularChanges(exchangeOrderMap, movingPriceLevel, tickSize);
                // 按当前的市场价去比对此时订单薄的市场价，差别较大，重新下单
                this.diffMarketPrice(exchangeOrderMap);
            } catch (Exception ex) {
                log.error("PriceCorrectionService.start error", ex);
            }
        }, 15000, 1000, TimeUnit.MILLISECONDS);
    }

    public void diffMarketPrice(Map<String, ExchangeOrder> exchangeOrderMap) {
        BigDecimal currentMarketPrice = this.getMarkerPriceInCache();
        if (currentMarketPrice == null) {
            log.error("PriceCorrectionService.start currentMarketPrice is null");
            return;
        }
        ExchangeOrder exchangeBuyOrder1 = exchangeOrderMap.get(this.createOrderKey(OrderSide.BUY, 1));
        ExchangeOrder exchangeBuyOrder2 = exchangeOrderMap.get(this.createOrderKey(OrderSide.BUY, 2));

        BigDecimal rangePrice = new BigDecimal(exchangeBuyOrder1.getPrice()).subtract(new BigDecimal(exchangeBuyOrder2.getPrice()));
        // 买一价加上固定的价差就是之前系统获得的市场价
        BigDecimal diffPrice = new BigDecimal(exchangeBuyOrder1.getPrice()).add(rangePrice).subtract(currentMarketPrice).abs();
        if (diffPrice.compareTo(threshold) > 0) {
            // 价差大于一定的阈值，需要触发重新下单
            log.error("PriceCorrectionService.start currentMarketPrice is not valid, currentMarketPrice is {}, exchangeOrderPrice is {}", currentMarketPrice, new BigDecimal(exchangeBuyOrder1.getPrice()).add(rangePrice));
        }
    }

    private BigDecimal getMarkerPriceInCache() {
        BookTickerEvent bookTickerEvent = marketFacade.lastTicker(ExchangeNameEnum.BYBIT.name(), symbol);
        if (bookTickerEvent != null) {
            return new BigDecimal(bookTickerEvent.getLastPrice());
        }
        return null;
    }

    private void checkOrderPriceDifference(Map<String, ExchangeOrder> exchangeOrderMap, OrderSide orderSide, int index, BigDecimal tickSize) {
        String preKey = createOrderKey(orderSide, index);
        String nextKey = createOrderKey(orderSide, index + 1);

        BigDecimal prePrice = getBigDecimalPrice(exchangeOrderMap.get(preKey));
        BigDecimal nextPrice = getBigDecimalPrice(exchangeOrderMap.get(nextKey));

        if (prePrice == null || nextPrice == null || !isPriceDifferenceValid(nextPrice, prePrice, tickSize)) {
            log.error("regularChanges orderType price difference is not a tick size, orderType: {} , prePrice: {}, nextPrice: {}", orderSide.toString(), prePrice, nextPrice);
            priceTierMoveService.createOrder();
        }
    }

    private String createOrderKey(OrderSide orderSide, int level) {
        return String.format(ORDER_LEVEL_KEY, orderSide.getValue(), level);
    }

    private BigDecimal getBigDecimalPrice(ExchangeOrder preOrder) {
        return preOrder == null ? null : new BigDecimal(preOrder.getPrice());
    }

    private boolean isPriceDifferenceValid(BigDecimal nextPrice, BigDecimal prePrice, BigDecimal tickSize) {
        boolean diffIsTickSize = BigDecimalUtil.isPriceDifferenceNotTickSize(prePrice, nextPrice, tickSize);
        if (!diffIsTickSize) {
            log.error("PriceCorrectionService.regularChanges preOrderPrice and nextOrderPrice is not tickSize,preOrderPrice is {},nextOrderPrice is {}", prePrice, nextPrice);
            return false;
        }
        return true;
    }
}
