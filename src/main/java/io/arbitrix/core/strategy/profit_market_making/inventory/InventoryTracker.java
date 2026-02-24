package io.arbitrix.core.strategy.profit_market_making.inventory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import io.arbitrix.core.common.enums.OrderSide;
import io.arbitrix.core.strategy.base.condition.ExecuteStrategyConditional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 库存跟踪器：跟踪每个交易对的 base coin 净持仓（以 USDT 等值估算）。
 *
 * <p>仓位偏斜（skew）定义：
 * <ul>
 *   <li>正值（> 0）：多头偏斜，base coin 过多，应收窄卖单价差、放宽买单价差</li>
 *   <li>负值（< 0）：空头偏斜，base coin 过少，应收窄买单价差、放宽卖单价差</li>
 *   <li>0：平衡，使用基础价差</li>
 * </ul>
 *
 * @author 3-X
 */
@Slf4j
@Component
@ExecuteStrategyConditional(executeStrategyName = "profit_market_making")
public class InventoryTracker {

    /**
     * 目标仓位比例：base coin 占总资产的 50%
     */
    private static final BigDecimal TARGET_RATIO = new BigDecimal("0.5");

    /**
     * 仓位偏斜阈值：超过 ±20% 触发调整
     */
    private static final BigDecimal SKEW_THRESHOLD = new BigDecimal("0.2");

    /**
     * 最大偏斜因子：最多将价差放宽/收窄到 2 倍
     */
    private static final BigDecimal MAX_SKEW_FACTOR = new BigDecimal("1.5");

    /**
     * symbol -> base coin 净持仓数量（正数 = 多头）
     */
    private final ConcurrentHashMap<String, BigDecimal> inventory = new ConcurrentHashMap<>();

    /**
     * 订单成交时更新持仓
     *
     * @param symbol   交易对（如 ETHUSDT）
     * @param side     方向
     * @param quantity 成交数量（base coin）
     */
    public void onOrderFilled(String symbol, OrderSide side, BigDecimal quantity) {
        BigDecimal delta = OrderSide.BUY.equals(side) ? quantity : quantity.negate();
        inventory.merge(symbol, delta, BigDecimal::add);
        log.debug("InventoryTracker.onOrderFilled: symbol={}, side={}, qty={}, newInventory={}",
                symbol, side, quantity, inventory.get(symbol));
    }

    /**
     * 计算库存偏斜调整因子，用于修正买卖单价差。
     *
     * <p>返回值含义：
     * <ul>
     *   <li>{@code buyFactor}：买单价差乘数（> 1 放宽，< 1 收窄）</li>
     *   <li>{@code sellFactor}：卖单价差乘数（> 1 放宽，< 1 收窄）</li>
     * </ul>
     *
     * @param symbol      交易对
     * @param midPrice    当前中间价
     * @param totalCapital 总资本（USDT）
     * @return 买卖因子对 [buyFactor, sellFactor]
     */
    public BigDecimal[] getSpreadFactors(String symbol, BigDecimal midPrice, BigDecimal totalCapital) {
        BigDecimal baseHolding = inventory.getOrDefault(symbol, BigDecimal.ZERO);
        if (baseHolding.compareTo(BigDecimal.ZERO) == 0 || midPrice.compareTo(BigDecimal.ZERO) == 0) {
            return new BigDecimal[]{BigDecimal.ONE, BigDecimal.ONE};
        }

        // base coin 持仓折算为 USDT
        BigDecimal baseValueUsdt = baseHolding.multiply(midPrice);

        // 当前 base 占比 = base USDT / 总资本
        BigDecimal currentRatio = baseValueUsdt.divide(totalCapital, 8, RoundingMode.HALF_UP);

        // 偏斜量 = 当前占比 - 目标占比（0.5）
        BigDecimal skew = currentRatio.subtract(TARGET_RATIO);

        // 偏斜未超过阈值，不调整
        if (skew.abs().compareTo(SKEW_THRESHOLD) < 0) {
            return new BigDecimal[]{BigDecimal.ONE, BigDecimal.ONE};
        }

        // 将偏斜映射到 [1.0, MAX_SKEW_FACTOR] 范围
        // 偏斜程度（0 ~ 1）= (|skew| - threshold) / (0.5 - threshold)
        BigDecimal skewDegree = skew.abs().subtract(SKEW_THRESHOLD)
                .divide(new BigDecimal("0.5").subtract(SKEW_THRESHOLD), 8, RoundingMode.HALF_UP)
                .min(BigDecimal.ONE);  // 上限1
        BigDecimal adjustment = BigDecimal.ONE.add(skewDegree.multiply(MAX_SKEW_FACTOR.subtract(BigDecimal.ONE)));

        BigDecimal buyFactor;
        BigDecimal sellFactor;

        if (skew.compareTo(BigDecimal.ZERO) > 0) {
            // 多头偏斜：base 太多 → 放宽买单价差（劝退买入），收窄卖单价差（鼓励卖出）
            buyFactor = adjustment;
            sellFactor = BigDecimal.ONE.divide(adjustment, 8, RoundingMode.HALF_UP);
        } else {
            // 空头偏斜：base 太少 → 收窄买单价差（鼓励买入），放宽卖单价差（劝退卖出）
            buyFactor = BigDecimal.ONE.divide(adjustment, 8, RoundingMode.HALF_UP);
            sellFactor = adjustment;
        }

        log.debug("InventoryTracker.getSpreadFactors: symbol={}, skew={}, buyFactor={}, sellFactor={}",
                symbol, skew, buyFactor, sellFactor);

        return new BigDecimal[]{buyFactor, sellFactor};
    }

    public BigDecimal getInventory(String symbol) {
        return inventory.getOrDefault(symbol, BigDecimal.ZERO);
    }
}
