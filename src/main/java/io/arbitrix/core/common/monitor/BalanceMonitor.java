package io.arbitrix.core.common.monitor;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import io.arbitrix.core.common.event.BalanceChangeEvent;
import io.arbitrix.core.common.monitor.listener.BalanceListener;
import io.arbitrix.core.common.response.AccountBalance;
import io.arbitrix.core.common.util.JacksonUtil;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Log4j2
@Component
public class BalanceMonitor implements BalanceListener {
    private static final String ACCOUNT_TYPE_FORMATTER = "%s_%s";
    /**
     * exchangeName -> balance
     */
    private final Map<String, AtomicReference<AccountBalance>> currentBalanceMap = new ConcurrentHashMap<>();

    public BalanceMonitor() {
    }


    @Override
    public void onBalanceChange(String exchangeName, BalanceChangeEvent balanceChangeEvent) {
        // TODO 2023/11/14 binance待实现
        log.info("onBalanceChange,exchangeName:{},balanceChangeEvent:{}", exchangeName, JacksonUtil.toJsonStr(balanceChangeEvent));
        String cacheKey = String.format(ACCOUNT_TYPE_FORMATTER, exchangeName, balanceChangeEvent.getAccountBalance().getAccountType());
        if (!currentBalanceMap.containsKey(cacheKey)) {
            AtomicReference<AccountBalance> currentBalanceReference = new AtomicReference<>(balanceChangeEvent.getAccountBalance());
            currentBalanceMap.put(cacheKey, currentBalanceReference);
            Gauge.builder("balance", currentBalanceReference, balance -> new BigDecimal(balance.get().getTotalWalletBalance()).doubleValue())
                    .tags("exchange", exchangeName)
                    .tags("balanceType", "totalWalletBalance", "accountType", currentBalanceReference.get().getAccountType())
                    .description("balance")
                    .register(Metrics.globalRegistry);
            Gauge.builder("balance", currentBalanceReference, balance -> new BigDecimal(balance.get().getTotalAvailableBalance()).doubleValue())
                    .tags("exchange", exchangeName)
                    .tags("balanceType", "totalAvailableBalance", "accountType", currentBalanceReference.get().getAccountType())
                    .description("balance")
                    .register(Metrics.globalRegistry);
            Gauge.builder("balance", currentBalanceReference, balance -> new BigDecimal(balance.get().getTotalEquity()).doubleValue())
                    .tags("exchange", exchangeName)
                    .tags("balanceType", "totalEquity", "accountType", currentBalanceReference.get().getAccountType())
                    .description("balance")
                    .register(Metrics.globalRegistry);
        } else {
            currentBalanceMap.get(cacheKey).set(balanceChangeEvent.getAccountBalance());
        }
    }

}
