package io.arbitrix.core.integration.bybit.rest.dto.res;

import lombok.Data;

import java.util.List;

@Data
public class SportTickerInfoRes {
    private String category;
    private List<SportTickerInfoResDetail> list;
}
