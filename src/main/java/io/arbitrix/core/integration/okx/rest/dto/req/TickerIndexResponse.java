package io.arbitrix.core.integration.okx.rest.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * @author jonathan.ji
 */
@Data
public class TickerIndexResponse {
    private Integer code;
    @JsonProperty("msg")
    private String message;
    private List<TickerIndex> data;
}
