package io.arbitrix.core.integration.bybit.rest.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.io.Serializable;
import java.util.List;

@Data
@Log4j2
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpotOpenOrderRes implements Serializable {

    private static final long serialVersionUID = -1L;
    /**
     * Product type
     */
    private String category;
    /**
     * next page cursor
     */
    private String nextPageCursor;

    /**
     * Order list
     */
    private List<SpotOpenOrderInfo> list;
}
