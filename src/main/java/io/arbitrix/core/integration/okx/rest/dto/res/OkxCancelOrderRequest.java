package io.arbitrix.core.integration.okx.rest.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OkxCancelOrderRequest {
    private String clOrdId;
    private String instId;
}
