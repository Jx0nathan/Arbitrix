package io.arbitrix.core.common.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.arbitrix.core.common.response.AccountBalance;

import java.util.Map;

/**
 * BalanceChangeEvent event for balance change
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class BalanceChangeEvent {
    private AccountBalance accountBalance;
}