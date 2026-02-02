package io.arbitrix.core.common.orderbook;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.arbitrix.core.common.event.OrderTradeUpdateEvent;

import java.util.List;

/**
 * @author jonathan.ji
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OwnOrderBook {

    /**
     * 锚定价格，当前批次订单的price依据的价格
     */
    private String anchorPrice;

    /**
     * 生成批次订单的时间
     */
    private Long createTime;

    /**
     * 生成的批次订单
     */
    private List<OrderTradeUpdateEvent> orderTradeUpdateEventList;

}
