package io.arbitrix.core.common.domain;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class LocalOpenOrderDetail {

    private String origClientOrderId;

    private BigDecimal price;

}
