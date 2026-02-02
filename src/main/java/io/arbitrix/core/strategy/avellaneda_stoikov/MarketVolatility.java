package io.arbitrix.core.strategy.avellaneda_stoikov;

import java.util.List;

public class MarketVolatility {

    public static double sigma(List<Double> prices) {
        double sum = 0.0;
        for (double price : prices) {
            sum += price;
        }
        double avgPrice = sum / prices.size();

        // 计算价格标准差
        double sumOfSquares = 0.0;
        for (double price : prices) {
            sumOfSquares += Math.pow(price - avgPrice, 2);
        }
        double stdDev = Math.sqrt(sumOfSquares / prices.size());

        // 计算波动率
        return stdDev / avgPrice;
    }

}
