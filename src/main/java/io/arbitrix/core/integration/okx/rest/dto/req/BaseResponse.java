package io.arbitrix.core.integration.okx.rest.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author jonathan.ji
 */
@Data
public class BaseResponse {
    private Integer code;
    @JsonProperty("msg")
    private String message;
}
