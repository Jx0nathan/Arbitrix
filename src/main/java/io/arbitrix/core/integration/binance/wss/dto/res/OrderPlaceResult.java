package io.arbitrix.core.integration.binance.wss.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jonathan.ji
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPlaceResult {
    /**
     * 交易对
     */
    private String symbol;

    /**
     * 系统的订单ID
     */
    private Long orderId;

    /**
     * OCO订单ID，否则为 -1
     */
    private Long orderListId;

    /**
     * 客户自己设置的ID
     */
    private String clientOrderId;

    /**
     * 交易的时间戳
     */
    private Long transactTime;

    /**
     * 订单价格
     */
    private String price;

    /**
     * 用户设置的原始订单数量
     */
    private String origQty;

    /**
     * 交易的订单数量
     */
    private String executedQty;

    /**
     * 累计交易的金额
     */
    private String cummulativeQuoteQty;

    /**
     * 订单状态
     */
    private String status;

    /**
     * 订单的时效方式
     */
    private String timeInForce;

    /**
     * 订单类型， 比如市价单，现价单等
     */
    private String type;

    /**
     * 订单方向，买还是卖
     */
    private String side;

    /**
     * 订单添加到 order book 的时间
     */
    private Long workingTime;

    /**
     * 自我交易预防模式
     */
    private String selfTradePreventionMode;
}
