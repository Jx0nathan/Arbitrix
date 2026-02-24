package io.arbitrix.core.strategy.avellaneda_stoikov;

import java.util.ArrayDeque;

/**
 * Avellaneda-Stoikov market making algorithm.
 *
 * <p>Uses Welford's online algorithm with a fixed sliding window to compute
 * mid-price mean and standard deviation in O(1) per update, avoiding
 * unbounded memory growth and O(n) recalculation on every tick.</p>
 */
public class ASAlgorithm {

    private static final int DEFAULT_WINDOW_SIZE = 200;

    // 策略参数
    private double inventory;
    private final double spread;
    private final double alpha;
    private final double beta;
    private final double gamma;
    private final double riskAversion;
    private final double referencePrice;
    private double buyPrice;
    private double sellPrice;

    // 固定大小滑动窗口，替换原来无限增长的 List
    private final int windowSize;
    private final ArrayDeque<Double> midPriceWindow;

    // Welford 在线算法维护的运行统计量（O(1) 更新）
    private int count = 0;
    private double runningMean = 0.0;
    private double runningM2 = 0.0;

    public ASAlgorithm(double inventory, double spread, double alpha, double beta,
                       double gamma, double riskAversion, double referencePrice) {
        this(inventory, spread, alpha, beta, gamma, riskAversion, referencePrice, DEFAULT_WINDOW_SIZE);
    }

    public ASAlgorithm(double inventory, double spread, double alpha, double beta,
                       double gamma, double riskAversion, double referencePrice, int windowSize) {
        this.inventory = inventory;
        this.spread = spread;
        this.alpha = alpha;
        this.beta = beta;
        this.gamma = gamma;
        this.riskAversion = riskAversion;
        this.referencePrice = referencePrice;
        this.buyPrice = referencePrice - spread / 2;
        this.sellPrice = referencePrice + spread / 2;
        this.windowSize = windowSize;
        this.midPriceWindow = new ArrayDeque<>(windowSize);
    }

    /**
     * 更新市场订单，使用 Welford 在线算法维护均值和方差。
     *
     * @param newInventory 最新持仓量
     */
    public void updateMarketOrder(double newInventory) {
        double midPrice = (buyPrice + sellPrice) / 2;

        // 若窗口已满，移除最旧的值并从统计量中撤销
        if (midPriceWindow.size() >= windowSize) {
            double oldest = midPriceWindow.pollFirst();
            removeFromWelford(oldest);
        }

        // 将新值加入窗口并更新统计量
        midPriceWindow.addLast(midPrice);
        addToWelford(midPrice);

        // 计算当前标准差（O(1)）
        double midPriceStd = (count > 1) ? Math.sqrt(runningM2 / (count - 1)) : 0.0;

        double lambda = alpha * Math.exp(-1 * beta * midPriceStd);
        double optimalSpread = gamma * Math.sqrt(midPriceStd);

        // 防止 optimalSpread 为0导致除零
        if (optimalSpread == 0) {
            inventory = newInventory;
            return;
        }

        double optimalBuyPrice = midPrice - optimalSpread / 2;
        double optimalSellPrice = midPrice + optimalSpread / 2;
        double optimalInventory = -1 * lambda * (optimalBuyPrice - referencePrice)
                / (2 * riskAversion * Math.pow(optimalSpread, 2));

        double deltaInventory = newInventory - inventory;
        double adjustedInventory = inventory + deltaInventory;
        double adjustedBuyPrice = buyPrice - lambda * deltaInventory / optimalSpread;
        double adjustedSellPrice = sellPrice + lambda * deltaInventory / optimalSpread;

        if (adjustedInventory > optimalInventory) {
            buyPrice = optimalBuyPrice;
            sellPrice = adjustedSellPrice;
        } else if (adjustedInventory < optimalInventory) {
            buyPrice = adjustedBuyPrice;
            sellPrice = optimalSellPrice;
        }

        inventory = newInventory;
    }

    public double getBuyPrice() {
        return buyPrice;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    // ── Welford 在线算法（增量更新）──────────────────────────────────

    /**
     * 加入新值，O(1) 更新均值和 M2（方差中间量）。
     */
    private void addToWelford(double value) {
        count++;
        double delta = value - runningMean;
        runningMean += delta / count;
        double delta2 = value - runningMean;
        runningM2 += delta * delta2;
    }

    /**
     * 撤销旧值（滑动窗口移除最旧数据），O(1) 更新均值和 M2。
     */
    private void removeFromWelford(double value) {
        if (count <= 1) {
            count = 0;
            runningMean = 0.0;
            runningM2 = 0.0;
            return;
        }
        double oldMean = runningMean;
        runningMean = (runningMean * count - value) / (count - 1);
        runningM2 -= (value - oldMean) * (value - runningMean);
        runningM2 = Math.max(runningM2, 0.0); // 防止浮点误差导致负值
        count--;
    }
}
