package io.arbitrix.core.integration.okx.rest.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import io.arbitrix.core.common.util.JacksonUtil;

import java.util.List;

/**
 * @author jonathan.ji
 */
@Data
public class ServerTimeResponse {
    private Integer code;
    @JsonProperty("msg")
    private String message;
    private List<ServerTime> data;

    public static ServerTimeResponse fromJson(String json) {
        return JacksonUtil.from(json, ServerTimeResponse.class);
    }
}
