package io.arbitrix.core.strategy.moving_grid;

import lombok.extern.log4j.Log4j2;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
public class GridService {

    private static final Grid grid = new Grid();

    public static Grid getGrid() {
        return grid;
    }

    public static void resetGrid(Grid grid, BigDecimal higherLine, BigDecimal lowerLine) {
        // 加锁
        grid.setHigherLine(higherLine);
        grid.setLowerLine(lowerLine);
    }

    public static void createGrids(BigDecimal startPrice, BigDecimal lower, BigDecimal upper, int gridNum) {
        List<GridDetail> gridDetailList = new ArrayList<>();

        BigDecimal gapPrice = getGapPrice(lower, upper, gridNum);
        for (int i = 0; i < gridNum + 1; i++) {
            GridDetail gridDetail = new GridDetail();
            gridDetail.setTriggerPrice(lower.add(gapPrice.multiply(BigDecimal.valueOf(i))));
            gridDetailList.add(gridDetail);
        }
        findClosestPriceRange(grid, startPrice, gridDetailList);
        grid.setGridDetailList(gridDetailList);
    }

    private static BigDecimal getGapPrice(BigDecimal lower, BigDecimal upper, int gridNum) {
        BigDecimal priceGapInTotal = upper.subtract(lower);
        // TODO 这个规则我先写死，可以要针对交易所的规则做特定的处理 ETHUSDT.tickSize = 0.01  BTCUSDT.tickSize = 0.1
        return priceGapInTotal.divide(BigDecimal.valueOf(gridNum), 2, RoundingMode.DOWN);
    }

    public static void findClosestPriceRange(Grid grid, BigDecimal priceA, List<GridDetail> gridsList) {
        // 默认是已经按价格做好了排序
        List<BigDecimal> priceList = gridsList.stream().map(GridDetail::getTriggerPrice).collect(Collectors.toList());

        BigDecimal lowerPrice = null;
        BigDecimal higherPrice = null;
        for (int i = 0; i < priceList.size() - 1; i++) {
            if (priceA.compareTo(priceList.get(i)) > 0 && priceA.compareTo(priceList.get(i + 1)) < 0) {
                lowerPrice = priceList.get(i);
                higherPrice = priceList.get(i + 1);
                break;
            }
        }

        // 如果价格A不在任何两个价格之间，可能是列表中的最小值或最大值
        if (lowerPrice == null || higherPrice == null) {
            log.error("price A is not in any two prices");
        }
        grid.setLowerLine(lowerPrice);
        grid.setHigherLine(higherPrice);
        log.info("price A set in " + lowerPrice + " and " + higherPrice);
    }
}
