package io.arbitrix.core.integration.okx.rest.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author jonathan.ji
 */
public enum OkxOrderStatus {
    @JsonProperty("live")
    Live,
    @JsonProperty("canceled")
    Canceled,
    @JsonProperty("filled")
    FullyFilled,
    @JsonProperty("partially_filled")
    PartiallyFilled
}
