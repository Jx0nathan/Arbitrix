package io.arbitrix.core.integration.bitget.rest.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import io.arbitrix.core.integration.bitget.rest.dto.res.BaseRes;
import io.arbitrix.core.integration.bitget.rest.dto.res.ServerTimeRes;

@FeignClient(value = "bitget", url = "${bitget.restBaseUrl}")
@RequestMapping("api/v2")
public interface MarketApi {


    @GetMapping("/public/time")
    BaseRes<ServerTimeRes> serverTime();
}
