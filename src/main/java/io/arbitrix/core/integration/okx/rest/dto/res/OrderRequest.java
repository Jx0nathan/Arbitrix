package io.arbitrix.core.integration.okx.rest.dto.res;

import lombok.Builder;
import lombok.Data;
import io.arbitrix.core.integration.okx.rest.enums.OkxSide;
import io.arbitrix.core.integration.okx.rest.enums.OkxOrderType;

@Data
@Builder
public class OrderRequest {

    private String symbol;
    private OkxSide side;
    private OkxOrderType type;
    private double price;
    private double quantity;
}
