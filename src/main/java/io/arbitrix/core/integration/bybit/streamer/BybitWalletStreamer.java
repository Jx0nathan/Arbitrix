package io.arbitrix.core.integration.bybit.streamer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import io.arbitrix.core.common.enums.ExchangeNameEnum;
import io.arbitrix.core.common.event.BalanceChangeEvent;
import io.arbitrix.core.common.monitor.WSSMonitor;
import io.arbitrix.core.common.monitor.listener.BalanceListener;
import io.arbitrix.core.common.response.AccountBalance;
import io.arbitrix.core.integration.bybit.wss.BybitWebSocketClient;
import io.arbitrix.core.integration.bybit.wss.dto.res.WSStreamBaseRes;
import io.arbitrix.core.strategy.base.condition.ExchangeConditional;
import io.arbitrix.core.utils.ExchangeMarketOpenUtilV2;
import io.arbitrix.core.common.util.JacksonUtil;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Component
@ExchangeConditional(exchangeName = "BYBIT")
public class BybitWalletStreamer {
    private static final String WALLET_TOPIC = "wallet";
    private final List<BalanceListener> balanceListeners;
    private final ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil;
    private final BybitWebSocketClient bybitWebSocketClient;

    public BybitWalletStreamer(List<BalanceListener> balanceListeners, ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil, BybitWebSocketClient bybitWebSocketClient) {
        this.balanceListeners = balanceListeners;
        this.exchangeMarketOpenUtil = exchangeMarketOpenUtil;
        this.bybitWebSocketClient = bybitWebSocketClient;
    }

    @PostConstruct
    private void startWalletStreaming() {
        if (!exchangeMarketOpenUtil.exchangeCanStart(ExchangeNameEnum.BYBIT)) {
            log.warn("BybitWalletStreamer.startWalletStreaming.exchangeCanStart is false and ignore");
            return;
        }
        List<String> subscribeReqs = Lists.newArrayList(WALLET_TOPIC);
        bybitWebSocketClient.subscribePrivate(subscribeReqs, response -> {
            WSSMonitor.receiveWalletEvent(ExchangeNameEnum.BYBIT.name());
            WSStreamBaseRes<List<io.arbitrix.core.integration.bybit.common.dto.WalletBalance>> orderInfoListRes = JacksonUtil.from(response, new TypeReference<>() {
            });
            List<io.arbitrix.core.integration.bybit.common.dto.WalletBalance> walletBalanceList = orderInfoListRes.getData();
            if (CollectionUtils.isEmpty(walletBalanceList)) {
                log.debug("BybitBookTickerStreamer.startBookTickerStreaming.bookTickerEvents is empty and ignore");
                return;
            }
            List<io.arbitrix.core.integration.bybit.common.dto.WalletBalance> unifiedList = walletBalanceList.stream().filter(walletBalance -> "UNIFIED".equals(walletBalance.getAccountType())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(unifiedList)) {
                log.debug("BybitWalletStreamer.startWalletStreaming.unifiedList is empty and ignore");
                return;
            }
            AccountBalance accountBalance = unifiedList.get(0).convert2CoinBalance();
            balanceListeners.forEach(balanceListener -> {
                balanceListener.onBalanceChange(ExchangeNameEnum.BYBIT.name(), new BalanceChangeEvent(accountBalance));
            });
        });
        log.info("BybitWalletStreamer.startWalletStreaming.subscribeReqs:{}", JacksonUtil.toJsonStr(subscribeReqs));
    }
}
