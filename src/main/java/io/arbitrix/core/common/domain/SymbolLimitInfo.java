package io.arbitrix.core.common.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 交易对限制信息
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
public class SymbolLimitInfo {

    private String category;

    private String symbol;

    /**
     * base coin precision
     */
    private String basePrecision;

    /**
     * quote coin precision
     */
    private String quotePrecision;

    /**
     * min qty
     */
    private String minOrderQty;

    /**
     * max qty
     */
    private String maxOrderQty;


    public String quoteCoinValue2BaseCoinValueFloor(BigDecimal price, String quoteCoinValue) {
        if (price.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("SymbolLimitInfo.quoteCoinValue2BaseCoinValueFloor price can not be zero");
        }
        BigDecimal quoteCoinValueDecimal = new BigDecimal(quoteCoinValue);
        BigDecimal basePrecisionDecimal = new BigDecimal(basePrecision);
        return quoteCoinValueDecimal.divide(price, basePrecisionDecimal.scale(), RoundingMode.DOWN).toPlainString();
    }
}
