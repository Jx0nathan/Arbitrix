package io.arbitrix.core.integration.okx.rest.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class OkxCancelOrderResponse {

    private Integer code;
    @JsonProperty("msg")
    private String message;
    private List<OkxCancelOrderResponseData> data;

}
