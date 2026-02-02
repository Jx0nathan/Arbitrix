package io.arbitrix.core.common.domain;

import lombok.Data;

import java.util.List;

@Data
public class MovingPriceOrderHolder {
    private List<ExchangeOrder> cancelOrderList;
    private List<ExchangeOrder> createOrderList;
}
