package io.arbitrix.core.integration.okx.rest.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OkxCancelOrderResponseData {

    @JsonProperty("clOrdId")
    private String clientSupliedId;
    @JsonProperty("ordId")
    private String orderId;
    @JsonProperty("sCode")
    private String successCode;
    @JsonProperty("sMsg")
    private String errorMessage;

}
