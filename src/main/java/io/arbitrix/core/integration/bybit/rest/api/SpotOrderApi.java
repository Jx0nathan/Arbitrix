package io.arbitrix.core.integration.bybit.rest.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import io.arbitrix.core.common.monitor.annotation.RecordRestApiDuration;
import io.arbitrix.core.integration.bybit.rest.dto.req.PlaceOrderReq;
import io.arbitrix.core.integration.bybit.rest.dto.req.SpotBatchCancelOrderReq;
import io.arbitrix.core.integration.bybit.rest.dto.req.SpotCancelOrderReq;
import io.arbitrix.core.integration.bybit.rest.dto.res.*;


@FeignClient(value = "bybit", url = "${bybit.restBaseUrl}")
@RequestMapping("/v5/order")
@RecordRestApiDuration
public interface SpotOrderApi {

    /**
     * place an order
     *
     * @param body
     * @return ResponseResult
     */
    @PostMapping("/create")
    BaseRestRes<SpotPlaceOrderRes> placeOrder(@RequestBody PlaceOrderReq body);


    /**
     * cancel the order
     *
     * @param body
     * @return ResponseResult
     */
    @PostMapping("/cancel")
    BaseRestRes<SpotCancelOrderRes> cancelOrder(@RequestBody SpotCancelOrderReq body);

    /**
     * Batch cancellation
     *
     * @param body
     * @return ResponseResult
     */
    @PostMapping("/cancel-batch")
    BaseRestRes<SpotBatchCancelOrderRes> cancelBatchOrder(@RequestBody SpotBatchCancelOrderReq body);

    /**
     * Obtain orders that have not been closed or partially closed but not cancelled
     *
     * @param category
     * @return ResponseResult
     */
    @GetMapping("/realtime")
    BaseRestRes<SpotOpenOrderRes> openOrders(@RequestParam String category, @RequestParam(required = false) String symbol);

    @GetMapping("/cancel-all")
    BaseRestRes<SpotCancelAllOrderRes> cancelAllopenOrders(@RequestParam String category, @RequestParam(required = false) String symbol);


}
