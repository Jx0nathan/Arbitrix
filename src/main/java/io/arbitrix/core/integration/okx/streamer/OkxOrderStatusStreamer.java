package io.arbitrix.core.integration.okx.streamer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import io.arbitrix.core.common.enums.ExchangeNameEnum;
import io.arbitrix.core.common.event.OrderTradeUpdateEvent;
import io.arbitrix.core.common.monitor.WSSMonitor;
import io.arbitrix.core.integration.okx.utils.OkxBridgeSystemUtil;
import io.arbitrix.core.integration.okx.utils.OkxClientOrderIdUtil;
import io.arbitrix.core.integration.okx.wss.dto.res.OkxSubscribeOrderInfo;
import io.arbitrix.core.integration.okx.wss.dto.res.OkxSubscribeOrderInfoDetail;
import io.arbitrix.core.strategy.base.action.OrderTradeUpdateListener;
import io.arbitrix.core.strategy.base.condition.ExchangeConditional;

import java.util.List;

/**
 * @author jonathan.ji
 */
@Component
@ExchangeConditional(exchangeName = "OKX")
@ConditionalOnBean(OrderTradeUpdateListener.class)
public class OkxOrderStatusStreamer {
    private final List<OrderTradeUpdateListener> orderTradeUpdateListenerList;
    private final OkxBridgeSystemUtil okxBridgeSystemUtil;

    public OkxOrderStatusStreamer(List<OrderTradeUpdateListener> orderTradeUpdateListenerList, OkxBridgeSystemUtil okxBridgeSystemUtil) {
        this.orderTradeUpdateListenerList = orderTradeUpdateListenerList;
        this.okxBridgeSystemUtil = okxBridgeSystemUtil;
    }

    public void startOrderStatusStreaming(OkxSubscribeOrderInfo okxSubscribeOrderInfo) {
        WSSMonitor.receiveOrderTradeEvent(ExchangeNameEnum.OKX.name());
        List<OkxSubscribeOrderInfoDetail> okxSubscribeOrderInfoDetailList = okxSubscribeOrderInfo.getData();
        for (OkxSubscribeOrderInfoDetail item : okxSubscribeOrderInfoDetailList) {
            // TODO 2023/11/13 区分是否全部成交,只有全部成交才需要推送
            OrderTradeUpdateEvent orderTradeUpdateEvent = this.conventToOrderTradeUpdateEvent(item);
            orderTradeUpdateListenerList.forEach(orderTradeUpdateListener -> {
                orderTradeUpdateListener.orderTradeUpdateEvent(ExchangeNameEnum.OKX.name(), orderTradeUpdateEvent);
            });
        }
    }

    private OrderTradeUpdateEvent conventToOrderTradeUpdateEvent(OkxSubscribeOrderInfoDetail orderInfoDetail) {
        OrderTradeUpdateEvent orderTradeUpdateEvent = new OrderTradeUpdateEvent();
        orderTradeUpdateEvent.setSymbol(orderInfoDetail.getInstId());
        if (StringUtils.isEmpty(orderInfoDetail.getFillTime())) {
            orderTradeUpdateEvent.setEventTime(System.currentTimeMillis());
        } else {
            orderTradeUpdateEvent.setEventTime(Long.parseLong(orderInfoDetail.getFillTime()));
        }
        orderTradeUpdateEvent.setNewClientOrderId(orderInfoDetail.getClOrdId());
        orderTradeUpdateEvent.setOrigClientOrderId(OkxClientOrderIdUtil.convertToArbitrix(orderInfoDetail.getClOrdId()));
        orderTradeUpdateEvent.setSide(okxBridgeSystemUtil.convertSideStr(orderInfoDetail.getSide()));
        orderTradeUpdateEvent.setOriginalQuantity(orderInfoDetail.getSz());
        orderTradeUpdateEvent.setPrice(orderInfoDetail.getPx());
        orderTradeUpdateEvent.setExecutionType(okxBridgeSystemUtil.convertExecutionType(orderInfoDetail.getState()));
        return orderTradeUpdateEvent;
    }
}
