package io.arbitrix.core.controller;

import net.openhft.affinity.AffinityLock;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import io.arbitrix.core.strategy.price_moving.PriceTierMoveService;
import io.arbitrix.core.strategy.profit_market_making.data.ProfitMarketMakingSpotOrderTradeDataManager;
import io.arbitrix.core.strategy.pure_market_making.data.PureMarketMakingSpotOrderTradeDataManager;
import io.arbitrix.core.common.util.JacksonUtil;

import static io.arbitrix.core.strategy.base.enums.ApplicationExecuteStrategyEnum.PROFIT_MARKET_MAKING;
import static io.arbitrix.core.strategy.base.enums.ApplicationExecuteStrategyEnum.PURE_MARKET_MAKING;

/**
 * @author jonathan.ji
 */
@RestController
public class GodHandController implements ApplicationContextAware {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private PriceTierMoveService priceTierMoveService;

    @GetMapping("/godHand/getMovingPriceOrderMap")
    public String getMovingPriceOrderMap() {
        return JacksonUtil.toJsonStr(priceTierMoveService.getPriceTierOrderMap());
    }

    @GetMapping("/godHand/getSystemCpus")
    public int getSystemCpus() {
        return AffinityLock.cpuLayout().cpus();
    }

    @GetMapping("/godHand/dumpAffinityLockInfo")
    public String dumpAffinityLockInfo() {
        return AffinityLock.dumpLocks();
    }

    @GetMapping("/godHand/getOrderPool")
    public String getOrderPool(String executeStrategyName) {
        if (PURE_MARKET_MAKING.getStrategyName().equalsIgnoreCase(executeStrategyName)) {
            PureMarketMakingSpotOrderTradeDataManager pureMarketMakingSpotOrderTradeDataManager = applicationContext.getBean("pureMarketMakingSpotOrderTradeDataManager", PureMarketMakingSpotOrderTradeDataManager.class);
            return JacksonUtil.toJsonStr(pureMarketMakingSpotOrderTradeDataManager.getOrderTradePool());
        }

        if (PROFIT_MARKET_MAKING.getStrategyName().equalsIgnoreCase(executeStrategyName)) {
            ProfitMarketMakingSpotOrderTradeDataManager profitMarketMakingSpotOrderTradeDataManager = applicationContext.getBean("profitOrderTradeDataManager", ProfitMarketMakingSpotOrderTradeDataManager.class);
            return JacksonUtil.toJsonStr(profitMarketMakingSpotOrderTradeDataManager.getOrderTradePool());
        }
        return "error";
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
