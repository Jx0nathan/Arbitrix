package io.arbitrix.core.common.domain;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author jonathan.ji
 */
@Data
public class MarketOpenExchangeInfo {

    private List<String> exchangeList;

    private Map<String, List<String>> symbolInfoMap;

}
