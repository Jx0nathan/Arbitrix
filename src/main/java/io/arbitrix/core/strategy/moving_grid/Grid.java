package io.arbitrix.core.strategy.moving_grid;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Grid {
    private BigDecimal higherLine;
    private BigDecimal lowerLine;
    private List<GridDetail> gridDetailList;

    public boolean isLowerPrice(BigDecimal price) {
        return gridDetailList.get(0).getTriggerPrice().compareTo(price) == 0;
    }

    public boolean isHigherPrice(BigDecimal price) {
        return gridDetailList.get(gridDetailList.size() - 1).getTriggerPrice().compareTo(price) == 0;
    }

    public Pair<BigDecimal, BigDecimal> getNextRangeByLower(BigDecimal lowerLine) {
        for (int i = 0; i < gridDetailList.size(); i++) {
            if (gridDetailList.get(i).getTriggerPrice().compareTo(lowerLine) == 0) {
                return Pair.of(gridDetailList.get(i).getTriggerPrice(), gridDetailList.get(i - 1).getTriggerPrice());
            }
        }
        return null;
    }

    public Pair<BigDecimal, BigDecimal> getNextRangeByUpper(BigDecimal upperLine) {
        for (int i = 0; i < gridDetailList.size(); i++) {
            if (gridDetailList.get(i).getTriggerPrice().compareTo(upperLine) == 0) {
                return Pair.of(gridDetailList.get(i + 1).getTriggerPrice(), gridDetailList.get(i).getTriggerPrice());
            }
        }
        return null;
    }
}
