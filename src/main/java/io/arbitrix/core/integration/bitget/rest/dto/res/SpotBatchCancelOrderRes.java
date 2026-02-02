package io.arbitrix.core.integration.bitget.rest.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpotBatchCancelOrderRes implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * Currency pair
     */
    private String symbol;

    /**
     * Order Ids
     */
    private List<SpotCancelOrderRes> resultList;

    /**
     * Client order Ids
     */
    private List<SpotCancelOrderFailureRes> failure;
}
