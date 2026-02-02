package io.arbitrix.core.integration.bybit.rest.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;
import io.arbitrix.core.common.response.AccountTradeFee;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jonathan.ji
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeeRateRes {

    /**
     * Product type. spot, option. Derivatives does not have this field
     */
    private String category;

    /**
     * Fee rate details
     */
    private List<FeeRateDetail> list;

    public AccountTradeFee convert2SingleAccountTradeFee() {
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        FeeRateDetail feeRateDetail = list.get(0);
        return feeRateDetail.convert2AccountTradeFee();
    }

    public List<AccountTradeFee> convert2AccountTradeFeesBySymbols(List<String> symbols) {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream().
                filter(feeRateDetail -> symbols.contains(feeRateDetail.getSymbol().toUpperCase())).
                map(FeeRateDetail::convert2AccountTradeFee).
                collect(Collectors.toList());
    }
}
