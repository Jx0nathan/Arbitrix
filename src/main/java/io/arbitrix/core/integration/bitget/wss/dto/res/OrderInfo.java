package io.arbitrix.core.integration.bitget.wss.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import io.arbitrix.core.common.event.OrderTradeUpdateEvent;
import io.arbitrix.core.integration.bitget.util.BitgetUtil;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author mcx
 * @date 2023/9/26
 * @description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderInfo {
    /**
     * 产品ID
     */
    private String instId;
    /**
     * 订单ID
     */
    private String ordId;
    /**
     * 客户订单ID
     */
    private String clOrdId;
    /**
     * 委托价格
     */
    private String px;
    /**
     * 委托数量
     */
    private String sz;
    /**
     * 买入金额，市价买入时返回
     */
    private String notional;
    /**
     * 订单类型 market：市价单 limit：限价单
     */
    private String ordType;
    /**
     * 订单有效期normal：普通委托post_only： 只做maker单fok：全部成交或立即取消单ioc：立即成交并取消剩余单
     */
    private String force;
    /**
     * 订单方向 buy：买 sell：卖
     */
    private String side;
    /**
     * 最新成交时间
     */
    private String fillTime;
    /**
     * 累计已成交数量
     */
    private String accFillSz;
    /**
     * 累计成交均价，如果成交数量为0，该字段也为0
     */
    private String avgPx;
    /**
     * 订单状态 init 插入DB, new:orderbook未成交 partial-fill 部分成交 full-fill：完全成交 cancelled：已撤单
     */
    private String status;
    /**
     * 数据来源enterPointSource
     */
    private String eps;
    /**
     * 订单创建时间
     */
    @JsonProperty("cTime")
    private Long createTime;
    /**
     * 订单更新时间
     */
    @JsonProperty("uTime")
    private Long updateTime;

    public static List<OrderTradeUpdateEvent> convert2OrderTradeUpdateEvents(List<OrderInfo> orderInfos) {
        if (CollectionUtils.isEmpty(orderInfos)) {
            return Collections.emptyList();
        }
        return orderInfos.stream().map(OrderInfo::convert2OrderTradeUpdateEvent).collect(Collectors.toList());
    }

    private static OrderTradeUpdateEvent convert2OrderTradeUpdateEvent(OrderInfo orderInfo) {
        OrderTradeUpdateEvent orderTradeUpdateEvent = new OrderTradeUpdateEvent();
        orderTradeUpdateEvent.setSymbol(BitgetUtil.symbolFromBitgetSpotToArbitrix(orderInfo.getInstId()));
        if (StringUtils.isEmpty(orderInfo.getFillTime())) {
            orderTradeUpdateEvent.setEventTime(System.currentTimeMillis());
        } else {
            orderTradeUpdateEvent.setEventTime(Long.parseLong(orderInfo.getFillTime()));
        }

        orderTradeUpdateEvent.setNewClientOrderId(orderInfo.getClOrdId());
        orderTradeUpdateEvent.setOrigClientOrderId(orderInfo.getClOrdId());
        orderTradeUpdateEvent.setSide(BitgetUtil.convertSide(orderInfo.getSide()));
        orderTradeUpdateEvent.setOriginalQuantity(orderInfo.getSz());
        orderTradeUpdateEvent.setPrice(orderInfo.getPx());
        orderTradeUpdateEvent.setExecutionType(BitgetUtil.convertExecutionType(orderInfo.getStatus()));
        return orderTradeUpdateEvent;
    }
}
