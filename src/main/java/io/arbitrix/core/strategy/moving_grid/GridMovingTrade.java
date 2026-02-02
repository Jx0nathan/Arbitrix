package io.arbitrix.core.strategy.moving_grid;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.util.List;

@Log4j2
public class GridMovingTrade {

    public void startTradeInGrid(BigDecimal marketPrice) {
        Grid grid = GridService.getGrid();

        BigDecimal lowerLine = grid.getLowerLine();
        BigDecimal higherLine = grid.getHigherLine();

        if (marketPrice.compareTo(lowerLine) <= 0 & !grid.isLowerPrice(lowerLine)) {
            log.info("买入，同时挂卖单");
            // 价格低于最低价格，买入，同时挂卖单，重置网格的上下线
            Pair<BigDecimal, BigDecimal> lowerPair = grid.getNextRangeByLower(lowerLine);
            GridService.resetGrid(grid, lowerPair.getLeft(), lowerPair.getRight());
            return;
        }

        if (marketPrice.compareTo(higherLine) >= 0 && !grid.isHigherPrice(higherLine)) {
            log.info("卖出，同时挂买单");
            // 价格高于最高价格，卖出，同时挂买单，重置网格的上下线
            Pair<BigDecimal, BigDecimal> higherPair = grid.getNextRangeByUpper(higherLine);
            GridService.resetGrid(grid, higherPair.getLeft(), higherPair.getRight());
            return;
        }
        log.info("什么都不做");
    }
}
