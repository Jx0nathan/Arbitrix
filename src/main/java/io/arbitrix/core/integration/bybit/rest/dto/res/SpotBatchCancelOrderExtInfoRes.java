package io.arbitrix.core.integration.bybit.rest.dto.res;

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
public class SpotBatchCancelOrderExtInfoRes implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * result list
     */
    private List<SpotCancelOrderExtInfoRes> list;

    public boolean isSuccess() {
        return list.stream().allMatch(SpotCancelOrderExtInfoRes::isSuccess);
    }
}
