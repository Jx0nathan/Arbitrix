package io.arbitrix.core.integration.bitget.rest.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Charles Meng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpotOpenOrderReq implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * Currency pair
     */
    private String symbol;
}
