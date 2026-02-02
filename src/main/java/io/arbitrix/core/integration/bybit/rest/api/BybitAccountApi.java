package io.arbitrix.core.integration.bybit.rest.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import io.arbitrix.core.common.monitor.annotation.RecordRestApiDuration;
import io.arbitrix.core.integration.bybit.rest.dto.res.BaseRestRes;
import io.arbitrix.core.integration.bybit.rest.dto.res.FeeRateRes;
import io.arbitrix.core.integration.bybit.rest.dto.res.WalletBalanceRes;


@FeignClient(value = "bybit", url = "${bybit.restBaseUrl}")
@RequestMapping("/v5/account")
@RecordRestApiDuration
public interface BybitAccountApi {

    /**
     * Get the trading fee rate.
     *
     * @param category Product type. spot, linear, inverse, option
     * @param symbol   name. Valid for linear, inverse, spot
     * @param baseCoin Base coin. SOL, BTC, ETH. Valid for option
     * @return ResponseResult
     */
    @GetMapping("/fee-rate")
    BaseRestRes<FeeRateRes> getTradeFee(@RequestParam String category, @RequestParam(required = false) String symbol, @RequestParam(required = false) String baseCoin);

    // only UNIFIED is allowed

    /**
     * wallet balance
     *
     * @param accountType default value is UNIFIED
     * @param coin        coin name,if not set,return all coin balance
     * @return
     */
    @GetMapping("/wallet-balance")
    BaseRestRes<WalletBalanceRes> getWalletBalance(@RequestParam(defaultValue = "UNIFIED") String accountType, @RequestParam(required = false) String coin);
}
