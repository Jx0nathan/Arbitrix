package io.arbitrix.core.integration.bybit.rest.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import io.arbitrix.core.common.response.AccountBalance;
import io.arbitrix.core.common.response.CoinBalance;
import io.arbitrix.core.integration.bybit.common.dto.WalletBalance;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletBalanceRes {

    private List<WalletBalance> list;


    public List<CoinBalance> convert2CoinBalances() {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.get(0).convert2CoinBalances();
    }
}
