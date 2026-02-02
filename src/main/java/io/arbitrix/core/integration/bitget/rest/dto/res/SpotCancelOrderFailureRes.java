package io.arbitrix.core.integration.bitget.rest.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpotCancelOrderFailureRes implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * Client order ID
     */
    private String clientOrderId;
    /**
     * errorMsg
     */
    private String errorMsg;
    /**
     * errorCode
     */
    private String errorCode;
}
