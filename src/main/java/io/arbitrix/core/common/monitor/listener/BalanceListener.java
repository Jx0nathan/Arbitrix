package io.arbitrix.core.common.monitor.listener;

import io.arbitrix.core.common.event.BalanceChangeEvent;

public interface BalanceListener {
    void onBalanceChange(String exchangeName, BalanceChangeEvent balanceChangeEvent);

}
