package io.arbitrix.core.common.enums;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Desired response type of NewOrder requests.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public enum NewOrderResponseType {
    ACK,
    RESULT,
    FULL
}

