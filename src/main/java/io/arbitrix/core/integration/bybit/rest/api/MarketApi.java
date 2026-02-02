package io.arbitrix.core.integration.bybit.rest.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import io.arbitrix.core.common.monitor.annotation.RecordRestApiDuration;
import io.arbitrix.core.integration.bybit.rest.dto.res.*;


@FeignClient(value = "bybit", url = "${bybit.restBaseUrl}")
@RequestMapping("/v5/market")
@RecordRestApiDuration
public interface MarketApi {

    @GetMapping("/time")
    BaseRestRes<ServerTimeRes> serverTime();

    /**
     * @param category 产品类型 spot,linear,inverse,option
     * @param symbol   产品名称
     */
    @GetMapping("/tickers")
    BaseRestRes<SportTickerInfoRes> getTickerInfo(@RequestParam String category, @RequestParam(required = false) String symbol);

}
