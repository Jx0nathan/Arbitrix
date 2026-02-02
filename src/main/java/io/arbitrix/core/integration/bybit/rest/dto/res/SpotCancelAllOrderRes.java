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
public class SpotCancelAllOrderRes implements Serializable {

    private static final long serialVersionUID = -1L;
    private String success;
    private List<SpotCancelOrderRes> list;
}
