package io.arbitrix.core.integration.okx.rest.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.arbitrix.core.integration.okx.rest.enums.OkxOrderType;
import io.arbitrix.core.integration.okx.rest.enums.OkxSide;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OkxOrderRequest {

    /**
     * 产品ID
     */
    private String instId;

    /**
     * 交易模式
     * 保证金模式：isolated：逐仓 ；cross：全仓
     * 非保证金模式：cash：非保证金
     */
    private String tdMode;

    /**
     * 客户自定义订单ID
     */
    private String clOrdId;

    /**
     * 订单方向 buy：买， sell：卖
     */
    private OkxSide side;

    /**
     * 订单类型
     * limit：限价单; market：市价单 ; post_only：只做maker单
     * fok：全部成交或立即取消; ioc：立即成交并取消剩余
     */
    private OkxOrderType ordType;

    /**
     * 委托价格，仅适用于limit、post_only、fok、ioc类型的订单
      */
    private String px;

    /**
     * 委托数量
     */
    private String sz;

}
