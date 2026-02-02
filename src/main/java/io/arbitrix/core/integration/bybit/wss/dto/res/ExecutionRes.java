package io.arbitrix.core.integration.bybit.wss.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import io.arbitrix.core.common.enums.OrderStatus;
import io.arbitrix.core.common.event.OrderTradeUpdateEvent;
import io.arbitrix.core.integration.bybit.util.BybitUtil;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionRes {
    private String symbol;
    private String execTime;
    @JsonProperty("orderLinkId")
    private String clientOrderId;
    private String orderId;
    private String side;
    private String orderPrice;
    private String orderQty;
    private String execType;
    private String leavesQty;

    public static List<OrderTradeUpdateEvent> convert2OrderTradeUpdateEvents(List<ExecutionRes> data) {
        if (CollectionUtils.isEmpty(data)) {
            return Collections.emptyList();
        }
        return data.stream().map(ExecutionRes::convert2OrderTradeUpdateEvent).collect(Collectors.toList());
    }

    private static OrderTradeUpdateEvent convert2OrderTradeUpdateEvent(ExecutionRes executionRes) {
        OrderTradeUpdateEvent orderTradeUpdateEvent = new OrderTradeUpdateEvent();
        orderTradeUpdateEvent.setSymbol(executionRes.getSymbol());
        if (StringUtils.isEmpty(executionRes.getExecTime())) {
            orderTradeUpdateEvent.setEventTime(System.currentTimeMillis());
        } else {
            orderTradeUpdateEvent.setEventTime(Long.parseLong(executionRes.getExecTime()));
        }

        orderTradeUpdateEvent.setNewClientOrderId(executionRes.getClientOrderId());
        orderTradeUpdateEvent.setOrigClientOrderId(executionRes.getClientOrderId());
        orderTradeUpdateEvent.setSide(BybitUtil.convertSideIn(executionRes.getSide()));
        orderTradeUpdateEvent.setOriginalQuantity(executionRes.getOrderQty());
        orderTradeUpdateEvent.setPrice(executionRes.getOrderPrice());
        orderTradeUpdateEvent.setExecutionType(BybitUtil.convertExecutionType(executionRes.getExecType()));
        BigDecimal leavesQty = new BigDecimal(executionRes.getLeavesQty());
        if (leavesQty.compareTo(BigDecimal.ZERO) == 0) {
            orderTradeUpdateEvent.setOrderStatus(OrderStatus.FILLED);
        }else {
            orderTradeUpdateEvent.setOrderStatus(OrderStatus.PARTIALLY_FILLED);
        }
        return orderTradeUpdateEvent;
    }
}