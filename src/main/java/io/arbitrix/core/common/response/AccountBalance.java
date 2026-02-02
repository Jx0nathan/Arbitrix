package io.arbitrix.core.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * account balance
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountBalance {
    private String accountType;
    private String totalWalletBalance;
    private String totalAvailableBalance;
    private String totalEquity;

}
