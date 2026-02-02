package io.arbitrix.core.strategy.avellaneda_stoikov;

import java.util.ArrayList;
import java.util.List;

public class ASAlgorithm {

    // 定义所需的变量
    private double inventory;
    private final double spread;
    private final double alpha;
    private final double beta;
    private final double gamma;
    private final double riskAversion;
    private final double referencePrice;
    private double buyPrice;
    private double sellPrice;
    private final List<Double> midPriceList;

    // 构造函数
    public ASAlgorithm(double inventory, double spread, double alpha, double beta, double gamma, double riskAversion, double referencePrice) {
        this.inventory = inventory;
        this.spread = spread;
        this.alpha = alpha;
        this.beta = beta;
        this.gamma = gamma;
        this.riskAversion = riskAversion;
        this.referencePrice = referencePrice;
        this.buyPrice = referencePrice - spread / 2;
        this.sellPrice = referencePrice + spread / 2;
        this.midPriceList = new ArrayList<>();
    }

    // 更新市场订单
    public void updateMarketOrder(double newInventory) {
        double midPrice = (buyPrice + sellPrice) / 2;
        midPriceList.add(midPrice);
        double midPriceMean = midPriceList.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double midPriceStd = Math.sqrt(midPriceList.stream().mapToDouble(x -> Math.pow(x - midPriceMean, 2)).average().orElse(0.0));
        double lambda = alpha * Math.exp(-1 * beta * midPriceStd);
        double optimalSpread = gamma * Math.sqrt(midPriceStd);
        double optimalBuyPrice = midPrice - optimalSpread / 2;
        double optimalSellPrice = midPrice + optimalSpread / 2;
        double optimalInventory = -1 * lambda * (optimalBuyPrice - referencePrice) / (2 * riskAversion * Math.pow(optimalSpread, 2));
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

}
