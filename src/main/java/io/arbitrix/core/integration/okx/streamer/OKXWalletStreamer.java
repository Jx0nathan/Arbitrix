package io.arbitrix.core.integration.okx.streamer;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import io.arbitrix.core.common.enums.ExchangeNameEnum;
import io.arbitrix.core.common.event.BalanceChangeEvent;
import io.arbitrix.core.common.monitor.WSSMonitor;
import io.arbitrix.core.common.monitor.listener.BalanceListener;
import io.arbitrix.core.common.response.AccountBalance;
import io.arbitrix.core.integration.okx.wss.dto.res.OkxBalanceData;
import io.arbitrix.core.integration.okx.wss.dto.res.OkxSubscribeAccount;
import io.arbitrix.core.strategy.base.condition.ExchangeConditional;

import java.util.List;

@Log4j2
@Component
@ExchangeConditional(exchangeName = "OKX")
public class OKXWalletStreamer {
    private final List<BalanceListener> balanceListeners;

    public OKXWalletStreamer(List<BalanceListener> balanceListeners) {
        this.balanceListeners = balanceListeners;
    }

    public void startOrderStatusStreaming(OkxSubscribeAccount okxSubscribeAccount, int priority) {
        WSSMonitor.receiveWalletEvent(ExchangeNameEnum.OKX.name());
        for (OkxBalanceData okxBalanceData : okxSubscribeAccount.getData()) {
            AccountBalance accountBalance = okxBalanceData.convert2AccountBalance(priority);
            balanceListeners.forEach(balanceListener -> {
                balanceListener.onBalanceChange(ExchangeNameEnum.OKX.name(), new BalanceChangeEvent(accountBalance));
            });
        }
    }
}
