package io.arbitrix.core.strategy.profit_market_making.order;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;
import io.arbitrix.core.common.enums.OrderLevel;
import io.arbitrix.core.strategy.base.condition.ExecuteStrategyConditional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 通过某些特征判断订单的深度分布
 *
 * @author jonathan.ji
 */
@Slf4j
@Component
@ExecuteStrategyConditional(executeStrategyName = "profit_market_making")
public class OrderBookDepthDistribution {
    private static Map<Integer, List<String>> UUID_MAP = new ConcurrentHashMap<>(8);
    private static final int UUID_BASE_LENGTH_LIMIT = 100;
    private static final int ORDER_LEVEL_LIMIT = 5;

    private final ProfitOrderPlaceStrategy profitOrderPlaceStrategy;

    public OrderBookDepthDistribution(ProfitOrderPlaceStrategy profitOrderPlaceStrategy){
        this.profitOrderPlaceStrategy = profitOrderPlaceStrategy;
    }

    static {
        ScheduledExecutorService scheduleCreateId = Executors.newScheduledThreadPool(1);
        scheduleCreateId.scheduleAtFixedRate(() -> {
            try {
                UUID_MAP.forEach((orderLevel, uuidList) -> {
                    int uuidSize = uuidList.size();
                    if (uuidSize < UUID_BASE_LENGTH_LIMIT) {
                        String uuidStr = UUID.randomUUID().toString();
                        int uuidToOrderLevel = Math.abs(uuidStr.hashCode() % ORDER_LEVEL_LIMIT);
                        if (uuidToOrderLevel == orderLevel) {
                            uuidList.add(uuidStr);
                        }
                    }
                });
            } catch (Throwable e) {
                log.error("OrderBookDepthDistribution.scheduleCreateId.error", e);
            }
        }, 0, 6, TimeUnit.MILLISECONDS);
    }

    public String getUuidByOrderLevel(int orderLevel) {
        List<OrderLevel> orderLevelList = profitOrderPlaceStrategy.getOrderLevelList();
        if (orderLevelList.size() == 1 && orderLevelList.get(0) == OrderLevel.FIRST_LEVEL) {
            return UUID.randomUUID().toString();
        } else {
            List<String> uuidList = UUID_MAP.get(orderLevel);
            if (CollectionUtils.isEmpty(uuidList)) {
                // 理论上是不会存在这个情况的话，如果有，我们暂时先不处理
                log.error("OrderBookDepthDistribution.getUuidByOrderLevel.error, orderLevel:{}", orderLevel);
                return null;
            }
            return uuidList.get(0);
        }
    }

    public int getOrderLevelByUUid(String uuid) {
        List<OrderLevel> orderLevelList = profitOrderPlaceStrategy.getOrderLevelList();
        if (orderLevelList.size() == 1 && orderLevelList.get(0) == OrderLevel.FIRST_LEVEL) {
            return OrderLevel.FIRST_LEVEL.getLevel();
        }
        return Math.abs(uuid.hashCode() % ORDER_LEVEL_LIMIT);
    }
}
