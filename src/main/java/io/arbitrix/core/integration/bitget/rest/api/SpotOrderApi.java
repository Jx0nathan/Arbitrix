package io.arbitrix.core.integration.bitget.rest.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import io.arbitrix.core.integration.bitget.rest.dto.req.SpotBatchCancelOrderReq;
import io.arbitrix.core.integration.bitget.rest.dto.req.SpotCancelOrderReq;
import io.arbitrix.core.integration.bitget.rest.dto.req.SpotOpenOrderReq;
import io.arbitrix.core.integration.bitget.rest.dto.req.SpotPlaceOrderReq;
import io.arbitrix.core.integration.bitget.rest.dto.res.*;

import java.util.List;

@FeignClient(value = "bitget", url = "${bitget.restBaseUrl}")
@RequestMapping("/api/spot/v1/trade")
public interface SpotOrderApi {

    /**
     * place an order
     *
     * @param body
     * @return ResponseResult
     */
    @PostMapping("/orders")
    BaseRes<SpotPlaceOrderRes> placeOrder(@RequestBody SpotPlaceOrderReq body);


    /**
     * cancel the order
     *
     * @param body
     * @return ResponseResult
     */
    @PostMapping("/cancel-order-v2")
    BaseRes<SpotCancelOrderRes> cancelOrder(@RequestBody SpotCancelOrderReq body);

    /**
     * Batch cancellation
     *
     * @param body
     * @return ResponseResult
     */
    @PostMapping("/cancel-batch-orders-v2")
    BaseRes<SpotBatchCancelOrderRes> cancelBatchOrder(@RequestBody SpotBatchCancelOrderReq body);

    /**
     * Obtain orders that have not been closed or partially closed but not cancelled
     *
     * @param body
     * @return ResponseResult
     */
    @PostMapping("/open-orders")
    BaseRes<List<SpotOpenOrderRes>> openOrders(@RequestBody SpotOpenOrderReq body);
}
