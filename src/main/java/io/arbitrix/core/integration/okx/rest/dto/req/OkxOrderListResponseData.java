package io.arbitrix.core.integration.okx.rest.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import io.arbitrix.core.integration.okx.rest.enums.OkxOrderStatus;
import io.arbitrix.core.integration.okx.rest.enums.OkxOrderType;
import io.arbitrix.core.integration.okx.rest.enums.OkxSide;

@Data
public class OkxOrderListResponseData {

    /**
     * 订单ID
     */
    private String ordId;

    /**
     * 客户自定义订单ID
     */
    private String clOrdId;

    private OkxOrderStatus state;

    /**
     * 产品ID "BTC-USDT"
     */
    private String instId;

    /**
     * 交易方向
     */
    private OkxSide side;

    /**
     * 订单类型
     */
    private OkxOrderType ordType;

    /**
     * 委托价格
     */
    private String px;

    /**
     * 委托数量
     */
    private String sz;

    /**
     * 订单创建时间，Unix时间戳的毫秒数格式
     */
    @JsonProperty("cTime")
    private String createTime;

}
