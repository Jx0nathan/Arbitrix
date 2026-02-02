package io.arbitrix.core.integration.okx.rest.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum OkxOrderType {

    /**
     * 市价单
     */
    @JsonProperty("market")
    MARKET,

    /**
     * 限价单
     */
    @JsonProperty("limit")
    LIMIT,

    @JsonProperty("post_only")
    POST_ONLY
}
