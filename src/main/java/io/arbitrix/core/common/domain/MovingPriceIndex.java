package io.arbitrix.core.common.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovingPriceIndex {
    private int level;
    private String ask1Price;
    private String bid1Price;
}
