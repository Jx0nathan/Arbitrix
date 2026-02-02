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
public class CoinBalance {
    private String coin;

    private String available;

    private String locked;

}
