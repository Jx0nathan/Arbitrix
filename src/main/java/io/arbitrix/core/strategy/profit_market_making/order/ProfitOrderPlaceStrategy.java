package io.arbitrix.core.strategy.profit_market_making.order;

import io.arbitrix.core.common.util.EnvUtil;
import org.springframework.stereotype.Component;
import io.arbitrix.core.common.enums.OrderLevel;
import io.arbitrix.core.strategy.base.enums.ProfitOrderPlaceStrategyEnum;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.arbitrix.core.strategy.base.enums.ProfitOrderPlaceStrategyEnum.*;

/**
 * 下单的策略
 *
 * @author jonathan.ji
 */
@Component
public class ProfitOrderPlaceStrategy {

    private static final String PROFIT_ORDER_PLACE_STRATEGY = "profit_order_place_strategy";
    private static final String FOLLOW_PRICE_BY_TOP_N_PARAMS = "follow_price_by_top_n_params";
    private static final String ORDER_PLACE_ORDER_SIZE = "order_place_order_size";

    private final ProfitOrderPlaceStrategyEnum orderPlaceStrategyName;
    private List<OrderLevel> orderLevelList;
    private final int orderSize;

    public ProfitOrderPlaceStrategy() {
        String placeStrategy = EnvUtil.getProperty(PROFIT_ORDER_PLACE_STRATEGY, ORDER_LEVEL_SPREAD_BY_BEST_PRICE.getStrategyTag());
        orderPlaceStrategyName = ProfitOrderPlaceStrategyEnum.getOrderPlaceStrategy(placeStrategy);

        if (ORDER_LEVEL_SPREAD_BY_BEST_PRICE.equals(orderPlaceStrategyName)) {
            orderLevelList = List.of(OrderLevel.FIRST_LEVEL);
        }

        if (FOLLOW_PRICE_BY_TOP_N.equals(orderPlaceStrategyName)) {
            String params = EnvUtil.getProperty(FOLLOW_PRICE_BY_TOP_N_PARAMS);
            orderLevelList = OrderLevel.warpOrderLevelList(Arrays.asList(params.split(",")));
        }

        if (ASK_PRICE_BASE_ON_BEST_BID_PRICE.equals(orderPlaceStrategyName)) {
            orderLevelList = List.of(OrderLevel.FIRST_LEVEL);
        }

        if (PRICE_BASE_ON_USDT_PRICE.equals(orderPlaceStrategyName)) {
            orderLevelList = List.of(OrderLevel.FIRST_LEVEL);
        }
        orderSize = Integer.parseInt(EnvUtil.getProperty(ORDER_PLACE_ORDER_SIZE, "1"));
    }

    public List<OrderLevel> getOrderLevelList() {
        return orderLevelList;
    }

    public ProfitOrderPlaceStrategyEnum getOrderPlaceStrategyName() {
        return orderPlaceStrategyName;
    }

    public Map<Integer, OrderLevel> getCurrentOrderLevelMap() {
        Map<Integer, OrderLevel> orderLevelMap = new HashMap<>(orderLevelList.size());
        for (OrderLevel item : orderLevelList) {
            orderLevelMap.put(item.getLevel(), item);
        }
        return orderLevelMap;
    }

    /**
     * 获取要放置订单的数量
     */
    public int getOrderPlaceQuantity() {
        return orderSize;
    }
}
