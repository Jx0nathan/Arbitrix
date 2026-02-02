package io.arbitrix.core.strategy.moving_grid;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 网格交易的原子数据
 *
 * @author jonathan.ji
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GridDetail {
    private BigDecimal triggerPrice;
}
